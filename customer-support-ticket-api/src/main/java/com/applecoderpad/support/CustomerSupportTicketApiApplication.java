package com.applecoderpad.support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CustomerSupportTicketApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(CustomerSupportTicketApiApplication.class, args);
  }
}
