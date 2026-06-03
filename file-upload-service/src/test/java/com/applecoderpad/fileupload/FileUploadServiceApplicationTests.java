package com.applecoderpad.fileupload;

import static org.assertj.core.api.Assertions.assertThat;

import com.applecoderpad.fileupload.dto.UploadResponse;
import com.applecoderpad.fileupload.service.FileUploadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest
class FileUploadServiceApplicationTests {
  @Autowired private FileUploadService uploads;

  @Test
  void acceptsMultipartUpload() {
    MockMultipartFile file =
        new MockMultipartFile("file", "hello.txt", "text/plain", "hello".getBytes());
    UploadResponse response = uploads.accept(file, "alice");
    assertThat(response.id()).isNotNull();
    assertThat(response.filename()).isEqualTo("hello.txt");
    assertThat(uploads.get(response.id()).checksum()).isNotBlank();
  }
}
