package com.applecoderpad.notification;

import com.applecoderpad.notification.model.DeliveryAttempt;
import java.util.concurrent.DelayQueue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

@SpringBootApplication
@EnableScheduling
public class NotificationServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(NotificationServiceApplication.class, args);
  }

  @Bean
  RestClient restClient() {
    return RestClient.create();
  }

  @Bean
  DelayQueue<DeliveryAttempt> notificationQueue() {
    return new DelayQueue<>();
  }
}
