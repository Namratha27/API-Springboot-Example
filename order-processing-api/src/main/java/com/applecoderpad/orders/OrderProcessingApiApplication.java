package com.applecoderpad.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class OrderProcessingApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(OrderProcessingApiApplication.class, args);
  }

  @Bean
  RestClient restClient() {
    return RestClient.create();
  }
}
