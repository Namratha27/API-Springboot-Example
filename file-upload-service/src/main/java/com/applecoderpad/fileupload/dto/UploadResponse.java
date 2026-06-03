package com.applecoderpad.fileupload.dto;

import com.applecoderpad.fileupload.model.StoredUpload;
import com.applecoderpad.fileupload.model.UploadStatus;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;

public record UploadResponse(
    UUID id,
    String owner,
    String filename,
    String contentType,
    long sizeBytes,
    String checksum,
    UploadStatus status,
    URI objectUri,
    String failureReason,
    Instant createdAt) {
  public static UploadResponse from(StoredUpload upload) {
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
        upload.createdAt());
  }
}
