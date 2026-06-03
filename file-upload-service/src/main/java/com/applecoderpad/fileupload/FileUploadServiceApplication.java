package com.applecoderpad.fileupload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestClient;

@SpringBootApplication
@EnableAsync
public class FileUploadServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(FileUploadServiceApplication.class, args);
  }

  @Bean
  RestClient restClient() {
    return RestClient.create();
  }
}
