package com.applecoderpad.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.applecoderpad.notification.dto.NotificationResponse;
import com.applecoderpad.notification.dto.SendNotificationRequest;
import com.applecoderpad.notification.model.Channel;
import com.applecoderpad.notification.service.NotificationService;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationServiceApplicationTests {
  @Autowired private NotificationService notifications;

  @Test
  void enqueuesNotificationForRequestedChannels() {
    NotificationResponse response =
        notifications.enqueue(
            new SendNotificationRequest(
                "dev@example.com", "Hello", "Body", Set.of(Channel.EMAIL, Channel.PUSH)));
    assertThat(response.id()).isNotNull();
    assertThat(response.deliveries()).hasSize(2);
  }
}
