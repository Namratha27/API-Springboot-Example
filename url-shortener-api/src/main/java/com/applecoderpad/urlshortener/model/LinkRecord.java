package com.applecoderpad.urlshortener.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "short_links")
public class LinkRecord {
  @Id private String code;

  @Column(nullable = false, length = 2048)
  private String originalUrl;

  @Column(nullable = false)
  private Instant createdAt;

  private Instant expiresAt;
  private long clicks;
  private volatile boolean disabled;

  protected LinkRecord() {}

  public LinkRecord(String code, String originalUrl, Instant createdAt, Instant expiresAt) {
    this.code = code;
    this.originalUrl = originalUrl;
    this.createdAt = createdAt;
    this.expiresAt = expiresAt;
  }

  public synchronized void incrementClicks() {
    clicks++;
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
    return clicks;
  }

  public boolean disabled() {
    return disabled;
  }
}
