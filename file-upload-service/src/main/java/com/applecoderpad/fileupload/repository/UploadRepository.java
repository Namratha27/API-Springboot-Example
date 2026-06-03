package com.applecoderpad.fileupload.repository;

import com.applecoderpad.fileupload.exception.NotFoundException;
import com.applecoderpad.fileupload.model.StoredUpload;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class UploadRepository {
  private final Map<UUID, StoredUpload> uploads = new ConcurrentHashMap<>();

  public void save(StoredUpload upload) {
    uploads.put(upload.id(), upload);
  }

  public StoredUpload get(UUID id) {
    StoredUpload upload = uploads.get(id);
    if (upload == null) {
      throw new NotFoundException("upload not found: " + id);
    }
    return upload;
  }

  public Collection<StoredUpload> findAll() {
    return uploads.values();
  }
}
