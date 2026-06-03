package com.applecoderpad.fileupload.service;

import com.applecoderpad.fileupload.dto.UploadResponse;
import com.applecoderpad.fileupload.exception.BadRequestException;
import com.applecoderpad.fileupload.model.StoredUpload;
import com.applecoderpad.fileupload.repository.UploadRepository;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collection;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService {
  private final UploadRepository repository;
  private final FileProcessingWorker worker;
  private final long maxBytes;

  public FileUploadService(
      UploadRepository repository,
      FileProcessingWorker worker,
      @Value("${file-upload.max-bytes}") long maxBytes) {
    this.repository = repository;
    this.worker = worker;
    this.maxBytes = maxBytes;
  }

  public UploadResponse accept(MultipartFile file, String owner) {
    if (file.isEmpty()) {
      throw new BadRequestException("file must not be empty");
    }
    if (file.getSize() > maxBytes) {
      throw new BadRequestException("file exceeds max size of " + maxBytes + " bytes");
    }
    byte[] bytes = read(file);
    StoredUpload upload =
        StoredUpload.queued(
            UUID.randomUUID(),
            owner,
            file.getOriginalFilename(),
            file.getContentType(),
            file.getSize(),
            sha256(bytes));
    repository.save(upload);
    worker.process(upload.id(), bytes);
    return UploadResponse.from(upload);
  }

  public UploadResponse get(UUID id) {
    return UploadResponse.from(repository.get(id));
  }

  public Collection<UploadResponse> list() {
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
      return Base64.getUrlEncoder()
          .withoutPadding()
          .encodeToString(MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }
}
