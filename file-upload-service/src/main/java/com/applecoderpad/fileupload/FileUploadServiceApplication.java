package com.applecoderpad.fileupload;

import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@EnableAsync
public class FileUploadServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileUploadServiceApplication.class, args);
    }

    @Bean
    RestClient restClient() {
        return RestClient.create();
    }
}

@RestController
@RequestMapping("/uploads")
class UploadController {
    private final FileUploadService uploads;

    UploadController(FileUploadService uploads) {
        this.uploads = uploads;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file,
                                          @RequestParam(defaultValue = "interview-user") String owner) {
        UploadResponse response = uploads.accept(file, owner);
        return ResponseEntity.created(URI.create("/uploads/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    UploadResponse get(@PathVariable UUID id) {
        return uploads.get(id);
    }

    @GetMapping
    Collection<UploadResponse> list() {
        return uploads.list();
    }
}

@Service
class FileUploadService {
    private final UploadRepository repository;
    private final FileProcessingWorker worker;
    private final long maxBytes;

    FileUploadService(UploadRepository repository,
                      FileProcessingWorker worker,
                      @Value("${file-upload.max-bytes}") long maxBytes) {
        this.repository = repository;
        this.worker = worker;
        this.maxBytes = maxBytes;
    }

    UploadResponse accept(MultipartFile file, String owner) {
        if (file.isEmpty()) {
            throw new BadRequestException("file must not be empty");
        }
        if (file.getSize() > maxBytes) {
            throw new BadRequestException("file exceeds max size of " + maxBytes + " bytes");
        }

        byte[] bytes = read(file);
        String checksum = sha256(bytes);
        StoredUpload upload = StoredUpload.queued(
                UUID.randomUUID(),
                owner,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                checksum
        );
        repository.save(upload);
        worker.process(upload.id(), bytes);
        return UploadResponse.from(upload);
    }

    UploadResponse get(UUID id) {
        return UploadResponse.from(repository.get(id));
    }

    Collection<UploadResponse> list() {
        return repository.findAll().stream().map(UploadResponse::from).toList();
    }

    private static byte[] read(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("could not read uploaded file");
        }
    }

    private static String sha256(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}

@Service
class FileProcessingWorker {
    private final UploadRepository repository;
    private final MalwareScannerGateway scanner;

    FileProcessingWorker(UploadRepository repository, MalwareScannerGateway scanner) {
        this.repository = repository;
        this.scanner = scanner;
    }

    @Async
    void process(UUID uploadId, byte[] bytes) {
        StoredUpload upload = repository.get(uploadId);
        upload.markProcessing();
        try {
            ScanDecision decision = scanner.scan(new ScanRequest(upload.id(), upload.filename(), upload.checksum(), bytes.length));
            if (decision.clean()) {
                upload.markAvailable(URI.create("object-storage://uploads/" + upload.id()));
            } else {
                upload.markRejected(decision.reason());
            }
        } catch (RuntimeException ex) {
            upload.markFailed(ex.getMessage());
        }
    }
}

@Service
class MalwareScannerGateway {
    private final RestClient restClient;
    private final boolean scannerEnabled;
    private final String scannerUrl;

    MalwareScannerGateway(RestClient restClient,
                          @Value("${file-upload.scanner-enabled}") boolean scannerEnabled,
                          @Value("${file-upload.scanner-url}") String scannerUrl) {
        this.restClient = restClient;
        this.scannerEnabled = scannerEnabled;
        this.scannerUrl = scannerUrl;
    }

    ScanDecision scan(ScanRequest request) {
        if (!scannerEnabled) {
            return new ScanDecision(true, "scanner disabled for local CoderPad run");
        }
        return restClient.post()
                .uri(scannerUrl)
                .body(request)
                .retrieve()
                .body(ScanDecision.class);
    }
}

@Repository
class UploadRepository {
    private final Map<UUID, StoredUpload> uploads = new ConcurrentHashMap<>();

    void save(StoredUpload upload) {
        uploads.put(upload.id(), upload);
    }

    StoredUpload get(UUID id) {
        StoredUpload upload = uploads.get(id);
        if (upload == null) {
            throw new NotFoundException("upload not found: " + id);
        }
        return upload;
    }

    Collection<StoredUpload> findAll() {
        return uploads.values();
    }
}

class StoredUpload {
    private final UUID id;
    private final String owner;
    private final String filename;
    private final String contentType;
    private final long sizeBytes;
    private final String checksum;
    private final Instant createdAt;
    private volatile UploadStatus status;
    private volatile URI objectUri;
    private volatile String failureReason;

    private StoredUpload(UUID id, String owner, String filename, String contentType, long sizeBytes, String checksum) {
        this.id = id;
        this.owner = owner;
        this.filename = filename == null ? "unnamed" : filename;
        this.contentType = contentType == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType;
        this.sizeBytes = sizeBytes;
        this.checksum = checksum;
        this.createdAt = Instant.now();
        this.status = UploadStatus.QUEUED;
    }

    static StoredUpload queued(UUID id, String owner, String filename, String contentType, long sizeBytes, String checksum) {
        return new StoredUpload(id, owner, filename, contentType, sizeBytes, checksum);
    }

    synchronized void markProcessing() {
        status = UploadStatus.PROCESSING;
    }

    synchronized void markAvailable(URI objectUri) {
        this.objectUri = objectUri;
        status = UploadStatus.AVAILABLE;
    }

    synchronized void markRejected(String reason) {
        this.failureReason = reason;
        status = UploadStatus.REJECTED;
    }

    synchronized void markFailed(String reason) {
        this.failureReason = reason;
        status = UploadStatus.FAILED;
    }

    UUID id() {
        return id;
    }

    String owner() {
        return owner;
    }

    String filename() {
        return filename;
    }

    String contentType() {
        return contentType;
    }

    long sizeBytes() {
        return sizeBytes;
    }

    String checksum() {
        return checksum;
    }

    Instant createdAt() {
        return createdAt;
    }

    UploadStatus status() {
        return status;
    }

    URI objectUri() {
        return objectUri;
    }

    String failureReason() {
        return failureReason;
    }
}

enum UploadStatus {
    QUEUED, PROCESSING, AVAILABLE, REJECTED, FAILED
}

record UploadResponse(UUID id,
                      String owner,
                      String filename,
                      String contentType,
                      long sizeBytes,
                      String checksum,
                      UploadStatus status,
                      URI objectUri,
                      String failureReason,
                      Instant createdAt) {
    static UploadResponse from(StoredUpload upload) {
        return new UploadResponse(
                upload.id(),
                upload.owner(),
                upload.filename(),
                upload.contentType(),
                upload.sizeBytes(),
                upload.checksum(),
                upload.status(),
                upload.objectUri(),
                upload.failureReason(),
                upload.createdAt()
        );
    }
}

record ScanRequest(UUID uploadId, @NotBlank String filename, String checksum, long sizeBytes) {
}

record ScanDecision(boolean clean, String reason) {
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException extends RuntimeException {
    NotFoundException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequestException extends RuntimeException {
    BadRequestException(String message) {
        super(message);
    }
}
