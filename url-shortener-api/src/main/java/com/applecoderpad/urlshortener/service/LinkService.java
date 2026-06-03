package com.applecoderpad.urlshortener.service;

import com.applecoderpad.urlshortener.dto.CreateLinkRequest;
import com.applecoderpad.urlshortener.dto.LinkResponse;
import com.applecoderpad.urlshortener.exception.BadRequestException;
import com.applecoderpad.urlshortener.exception.GoneException;
import com.applecoderpad.urlshortener.model.LinkRecord;
import com.applecoderpad.urlshortener.repository.LinkRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class LinkService {
  private static final char[] BASE62 =
      "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
  private static final Pattern CODE_PATTERN = Pattern.compile("[A-Za-z0-9_-]{4,32}");
  private final LinkRepository repository;
  private final SecureRandom random = new SecureRandom();

  public LinkService(LinkRepository repository) {
    this.repository = repository;
  }

  public LinkResponse create(CreateLinkRequest request) {
    String code =
        request.customAlias() == null || request.customAlias().isBlank()
            ? generateCode()
            : validateAlias(request.customAlias());
    LinkRecord link =
        new LinkRecord(code, request.originalUrl(), Instant.now(), request.expiresAt());
    repository.insert(link);
    return LinkResponse.from(link);
  }

  public LinkResponse resolve(String code) {
    LinkRecord link = repository.get(code);
    if (link.disabled()) throw new GoneException("link disabled");
    if (link.expiresAt() != null && !link.expiresAt().isAfter(Instant.now()))
      throw new GoneException("link expired");
    link.incrementClicks();
    return LinkResponse.from(link);
  }

  public LinkResponse get(String code) {
    return LinkResponse.from(repository.get(code));
  }

  public Collection<LinkResponse> list() {
    return repository.findAll().stream().map(LinkResponse::from).toList();
  }

  public void disable(String code) {
    repository.get(code).disable();
  }

  private String generateCode() {
    for (int attempt = 0; attempt < 10; attempt++) {
      String candidate = randomBase62(7);
      if (!repository.exists(candidate)) return candidate;
    }
    return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
  }

  private String randomBase62(int length) {
    StringBuilder builder = new StringBuilder(length);
    for (int i = 0; i < length; i++) builder.append(BASE62[random.nextInt(BASE62.length)]);
    return builder.toString();
  }

  private static String validateAlias(String alias) {
    if (!CODE_PATTERN.matcher(alias).matches())
      throw new BadRequestException("customAlias must be 4-32 URL-safe characters");
    return alias;
  }
}
