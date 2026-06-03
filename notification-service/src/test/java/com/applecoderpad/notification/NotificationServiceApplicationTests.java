package com.applecoderpad.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.applecoderpad.notification.dto.NotificationResponse;
import com.applecoderpad.notification.dto.SendNotificationRequest;
import com.applecoderpad.notification.model.Channel;
import com.applecoderpad.notification.model.DeliveryStatus;
import com.applecoderpad.notification.service.NotificationService;
import com.applecoderpad.notification.service.NotificationWorker;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationServiceApplicationTests {
  @Autowired private NotificationService notifications;
  @Autowired private NotificationWorker worker;

  @Test
  void enqueuesNotificationForRequestedChannels() {
    NotificationResponse response =
        notifications.enqueue(
            new SendNotificationRequest(
                "dev@example.com", "Hello", "Body", Set.of(Channel.EMAIL, Channel.PUSH)));
    assertThat(response.id()).isNotNull();
    assertThat(response.deliveries()).hasSize(2);
  }

  @Test
  void workerMarksDryRunDeliveriesAsSent() {
    NotificationResponse response =
        notifications.enqueue(
            new SendNotificationRequest("dev2@example.com", "Deploy", "Done", Set.of(Channel.SMS)));
    worker.drain();

    assertThat(notifications.get(response.id()).deliveries())
        .singleElement()
        .satisfies(delivery -> assertThat(delivery.status()).isEqualTo(DeliveryStatus.SENT));
  }
}
