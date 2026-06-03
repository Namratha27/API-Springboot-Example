package com.applecoderpad.urlshortener.dto;

import com.applecoderpad.urlshortener.model.LinkRecord;
import java.time.Instant;

public record LinkResponse(
    String code,
    String shortPath,
    String originalUrl,
    Instant createdAt,
    Instant expiresAt,
    long clicks,
    boolean disabled) {
  public static LinkResponse from(LinkRecord link) {
    return new LinkResponse(
        link.code(),
        "/" + link.code(),
        link.originalUrl(),
        link.createdAt(),
        link.expiresAt(),
        link.clicks(),
        link.disabled());
  }
}
