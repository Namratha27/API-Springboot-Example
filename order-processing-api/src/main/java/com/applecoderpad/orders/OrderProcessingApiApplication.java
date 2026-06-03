package com.applecoderpad.orders;

import java.time.Duration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class OrderProcessingApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(OrderProcessingApiApplication.class, args);
  }

  @Bean
  RestClient restClient() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(2));
    requestFactory.setReadTimeout(Duration.ofSeconds(5));
    return RestClient.builder().requestFactory(requestFactory).build();
  }
}
