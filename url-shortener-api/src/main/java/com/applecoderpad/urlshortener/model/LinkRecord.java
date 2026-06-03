package com.applecoderpad.urlshortener.model;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class LinkRecord {
  private final String code;
  private final String originalUrl;
  private final Instant createdAt;
  private final Instant expiresAt;
  private final AtomicLong clicks = new AtomicLong();
  private volatile boolean disabled;

  public LinkRecord(String code, String originalUrl, Instant createdAt, Instant expiresAt) {
    this.code = code;
    this.originalUrl = originalUrl;
    this.createdAt = createdAt;
    this.expiresAt = expiresAt;
  }

  public void incrementClicks() {
    clicks.incrementAndGet();
  }

  public void disable() {
    disabled = true;
  }

  public String code() {
    return code;
  }

  public String originalUrl() {
    return originalUrl;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant expiresAt() {
    return expiresAt;
  }

  public long clicks() {
    return clicks.get();
  }

  public boolean disabled() {
    return disabled;
  }
}
