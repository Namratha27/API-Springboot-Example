package com.applecoderpad.fileupload.repository;

import com.applecoderpad.fileupload.model.StoredUpload;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredUploadJpaRepository extends JpaRepository<StoredUpload, UUID> {}
