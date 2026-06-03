package com.applecoderpad.urlshortener;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootApplication
public class UrlShortenerApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerApiApplication.class, args);
    }
}

@RestController
class LinkController {
    private final LinkService links;

    LinkController(LinkService links) {
        this.links = links;
    }

    @PostMapping("/links")
    ResponseEntity<LinkResponse> create(@Valid @RequestBody CreateLinkRequest request) {
        LinkResponse response = links.create(request);
        return ResponseEntity.created(URI.create("/links/" + response.code())).body(response);
    }

    @GetMapping("/links/{code}")
    LinkResponse get(@PathVariable String code) {
        return links.get(code);
    }

    @GetMapping("/links")
    Collection<LinkResponse> list() {
        return links.list();
    }

    @DeleteMapping("/links/{code}")
    ResponseEntity<Void> disable(@PathVariable String code) {
        links.disable(code);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{code}")
    ResponseEntity<Void> redirect(@PathVariable String code) {
        LinkResponse link = links.resolve(code);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, link.originalUrl())
                .build();
    }
}

@Service
class LinkService {
    private static final char[] BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final java.util.regex.Pattern CODE_PATTERN = java.util.regex.Pattern.compile("[A-Za-z0-9_-]{4,32}");

    private final LinkRepository repository;
    private final SecureRandom random = new SecureRandom();

    LinkService(LinkRepository repository) {
        this.repository = repository;
    }

    LinkResponse create(CreateLinkRequest request) {
        String code = request.customAlias() == null || request.customAlias().isBlank()
                ? generateCode()
                : validateAlias(request.customAlias());

        LinkRecord link = new LinkRecord(
                code,
                request.originalUrl(),
                Instant.now(),
                request.expiresAt(),
                false
        );
        repository.insert(link);
        return LinkResponse.from(link);
    }

    LinkResponse resolve(String code) {
        LinkRecord link = repository.get(code);
        if (link.disabled()) {
            throw new GoneException("link disabled");
        }
        if (link.expiresAt() != null && !link.expiresAt().isAfter(Instant.now())) {
            throw new GoneException("link expired");
        }
        link.incrementClicks();
        return LinkResponse.from(link);
    }

    LinkResponse get(String code) {
        return LinkResponse.from(repository.get(code));
    }

    Collection<LinkResponse> list() {
        return repository.findAll().stream().map(LinkResponse::from).toList();
    }

    void disable(String code) {
        repository.get(code).disable();
    }

    private String generateCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String candidate = randomBase62(7);
            if (!repository.exists(candidate)) {
                return candidate;
            }
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private String randomBase62(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(BASE62[random.nextInt(BASE62.length)]);
        }
        return builder.toString();
    }

    private static String validateAlias(String alias) {
        if (!CODE_PATTERN.matcher(alias).matches()) {
            throw new BadRequestException("customAlias must be 4-32 URL-safe characters");
        }
        return alias;
    }
}

@Repository
class LinkRepository {
    private final Map<String, LinkRecord> byCode = new ConcurrentHashMap<>();

    void insert(LinkRecord link) {
        LinkRecord previous = byCode.putIfAbsent(link.code(), link);
        if (previous != null) {
            throw new ConflictException("short code already exists");
        }
    }

    LinkRecord get(String code) {
        LinkRecord link = byCode.get(code);
        if (link == null) {
            throw new NotFoundException("link not found: " + code);
        }
        return link;
    }

    boolean exists(String code) {
        return byCode.containsKey(code);
    }

    Collection<LinkRecord> findAll() {
        return byCode.values();
    }
}

class LinkRecord {
    private final String code;
    private final String originalUrl;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final AtomicLong clicks = new AtomicLong();
    private volatile boolean disabled;

    LinkRecord(String code, String originalUrl, Instant createdAt, Instant expiresAt, boolean disabled) {
        this.code = code;
        this.originalUrl = originalUrl;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.disabled = disabled;
    }

    void incrementClicks() {
        clicks.incrementAndGet();
    }

    void disable() {
        disabled = true;
    }

    String code() {
        return code;
    }

    String originalUrl() {
        return originalUrl;
    }

    Instant createdAt() {
        return createdAt;
    }

    Instant expiresAt() {
        return expiresAt;
    }

    long clicks() {
        return clicks.get();
    }

    boolean disabled() {
        return disabled;
    }
}

record CreateLinkRequest(@NotBlank @Pattern(regexp = "https?://.+", message = "must start with http:// or https://") String originalUrl,
                         String customAlias,
                         @Future Instant expiresAt) {
}

record LinkResponse(String code,
                    String shortPath,
                    String originalUrl,
                    Instant createdAt,
                    Instant expiresAt,
                    long clicks,
                    boolean disabled) {
    static LinkResponse from(LinkRecord link) {
        return new LinkResponse(
                link.code(),
                "/" + link.code(),
                link.originalUrl(),
                link.createdAt(),
                link.expiresAt(),
                link.clicks(),
                link.disabled()
        );
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequestException extends RuntimeException {
    BadRequestException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException extends RuntimeException {
    NotFoundException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.CONFLICT)
class ConflictException extends RuntimeException {
    ConflictException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.GONE)
class GoneException extends RuntimeException {
    GoneException(String message) {
        super(message);
    }
}
