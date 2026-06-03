package com.applecoderpad.fileupload.controller;

import com.applecoderpad.fileupload.dto.UploadResponse;
import com.applecoderpad.fileupload.service.FileUploadService;
import java.net.URI;
import java.util.Collection;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/uploads")
public class UploadController {
  private final FileUploadService uploadService;

  public UploadController(FileUploadService uploadService) {
    this.uploadService = uploadService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UploadResponse> upload(
      @RequestParam("file") MultipartFile file,
      @RequestParam(defaultValue = "interview-user") String owner) {
    UploadResponse response = uploadService.accept(file, owner);
    return ResponseEntity.created(URI.create("/uploads/" + response.id())).body(response);
  }

  @GetMapping("/{id}")
  public UploadResponse get(@PathVariable UUID id) {
    return uploadService.get(id);
  }

  @GetMapping
  public Collection<UploadResponse> list() {
    return uploadService.list();
  }
}
