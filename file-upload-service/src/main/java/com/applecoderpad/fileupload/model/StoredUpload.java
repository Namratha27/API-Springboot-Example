package com.applecoderpad.fileupload.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.MediaType;

@Entity
@Table(name = "stored_uploads")
public class StoredUpload {
  @Id private UUID id;

  @Column(nullable = false)
  private String owner;

  @Column(nullable = false)
  private String filename;

  @Column(nullable = false)
  private String contentType;

  @Column(nullable = false)
  private long sizeBytes;

  @Column(nullable = false, length = 96)
  private String checksum;

  @Column(nullable = false)
  private Instant createdAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private volatile UploadStatus status;

  @Column(length = 1024)
  private volatile String objectUri;

  @Column(length = 1024)
  private volatile String failureReason;

  protected StoredUpload() {}

  private StoredUpload(
      UUID id, String owner, String filename, String contentType, long sizeBytes, String checksum) {
    this.id = id;
    this.owner = owner;
    this.filename = filename == null ? "unnamed" : filename;
    this.contentType = contentType == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType;
    this.sizeBytes = sizeBytes;
    this.checksum = checksum;
    this.createdAt = Instant.now();
    this.status = UploadStatus.QUEUED;
  }

  public static StoredUpload queued(
      UUID id, String owner, String filename, String contentType, long sizeBytes, String checksum) {
    return new StoredUpload(id, owner, filename, contentType, sizeBytes, checksum);
  }

  public synchronized void markProcessing() {
    status = UploadStatus.PROCESSING;
  }

  public synchronized void markAvailable(URI objectUri) {
    this.objectUri = objectUri == null ? null : objectUri.toString();
    status = UploadStatus.AVAILABLE;
  }

  public synchronized void markRejected(String reason) {
    this.failureReason = reason;
    status = UploadStatus.REJECTED;
  }

  public synchronized void markFailed(String reason) {
    this.failureReason = reason;
    status = UploadStatus.FAILED;
  }

  public UUID id() {
    return id;
  }

  public String owner() {
    return owner;
  }

  public String filename() {
    return filename;
  }

  public String contentType() {
    return contentType;
  }

  public long sizeBytes() {
    return sizeBytes;
  }

  public String checksum() {
    return checksum;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public UploadStatus status() {
    return status;
  }

  public URI objectUri() {
    return objectUri == null ? null : URI.create(objectUri);
  }

  public String failureReason() {
    return failureReason;
  }
}
