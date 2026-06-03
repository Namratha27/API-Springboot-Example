package com.applecoderpad.fileupload.model;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.MediaType;

public class StoredUpload {
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
    this.objectUri = objectUri;
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
    return objectUri;
  }

  public String failureReason() {
    return failureReason;
  }
}
