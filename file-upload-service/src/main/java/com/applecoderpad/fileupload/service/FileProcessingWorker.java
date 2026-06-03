package com.applecoderpad.fileupload.service;

import com.applecoderpad.fileupload.dto.ScanDecision;
import com.applecoderpad.fileupload.dto.ScanRequest;
import com.applecoderpad.fileupload.model.StoredUpload;
import com.applecoderpad.fileupload.repository.UploadRepository;
import java.net.URI;
import java.util.UUID;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class FileProcessingWorker {
  private final UploadRepository repository;
  private final MalwareScannerClient scannerClient;

  public FileProcessingWorker(UploadRepository repository, MalwareScannerClient scannerClient) {
    this.repository = repository;
    this.scannerClient = scannerClient;
  }

  @Async
  public void process(UUID uploadId, byte[] bytes) {
    StoredUpload upload = repository.get(uploadId);
    upload.markProcessing();
    try {
      ScanDecision decision =
          scannerClient.scan(
              new ScanRequest(upload.id(), upload.filename(), upload.checksum(), bytes.length));
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
