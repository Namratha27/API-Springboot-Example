package com.applecoderpad.notification.service;

import com.applecoderpad.notification.dto.ProviderResponse;
import com.applecoderpad.notification.model.DeliveryAttempt;
import com.applecoderpad.notification.model.DeliveryRecord;
import com.applecoderpad.notification.model.DeliveryStatus;
import com.applecoderpad.notification.model.NotificationRecord;
import com.applecoderpad.notification.repository.NotificationRepository;
import java.util.concurrent.DelayQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NotificationWorker {
  private final NotificationRepository repository;
  private final DelayQueue<DeliveryAttempt> queue;
  private final ProviderGateway providerGateway;
  private final int maxAttempts;

  public NotificationWorker(
      NotificationRepository repository,
      DelayQueue<DeliveryAttempt> queue,
      ProviderGateway providerGateway,
      @Value("${notifications.max-attempts}") int maxAttempts) {
    this.repository = repository;
    this.queue = queue;
    this.providerGateway = providerGateway;
    this.maxAttempts = maxAttempts;
  }

  @Scheduled(fixedDelay = 500)
  public void drain() {
    DeliveryAttempt attempt;
    while ((attempt = queue.poll()) != null) {
      DeliveryRecord delivery = repository.getDelivery(attempt.deliveryId());
      if (delivery.status() == DeliveryStatus.SENT) continue;
      NotificationRecord notification = repository.get(delivery.notificationId());
      try {
        delivery.markSending(attempt.attempt());
        ProviderResponse response = providerGateway.deliver(notification, delivery.channel());
        delivery.markSent(response.providerMessageId());
      } catch (RuntimeException ex) {
        delivery.markFailed(ex.getMessage());
        if (attempt.attempt() < maxAttempts) queue.offer(attempt.next());
      }
    }
  }
}
