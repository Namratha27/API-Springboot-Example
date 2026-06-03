package com.applecoderpad.notification.service;

import com.applecoderpad.notification.dto.NotificationResponse;
import com.applecoderpad.notification.dto.SendNotificationRequest;
import com.applecoderpad.notification.model.Channel;
import com.applecoderpad.notification.model.DeliveryAttempt;
import com.applecoderpad.notification.model.DeliveryRecord;
import com.applecoderpad.notification.model.NotificationRecord;
import com.applecoderpad.notification.repository.NotificationRepository;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.DelayQueue;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
  private final NotificationRepository repository;
  private final DelayQueue<DeliveryAttempt> queue;

  public NotificationService(NotificationRepository repository, DelayQueue<DeliveryAttempt> queue) {
    this.repository = repository;
    this.queue = queue;
  }

  public NotificationResponse enqueue(SendNotificationRequest request) {
    UUID notificationId = UUID.randomUUID();
    Set<Channel> channels =
        request.channels() == null || request.channels().isEmpty()
            ? EnumSet.of(Channel.EMAIL)
            : request.channels();
    List<DeliveryRecord> deliveries =
        channels.stream()
            .map(channel -> DeliveryRecord.create(UUID.randomUUID(), notificationId, channel))
            .toList();
    NotificationRecord notification =
        new NotificationRecord(
            notificationId,
            request.recipient(),
            request.subject(),
            request.body(),
            Instant.now(),
            deliveries);
    repository.save(notification);
    deliveries.forEach(delivery -> queue.offer(DeliveryAttempt.now(delivery.id(), 1)));
    return NotificationResponse.from(notification);
  }

  public NotificationResponse get(UUID id) {
    return NotificationResponse.from(repository.get(id));
  }

  public Collection<NotificationResponse> list() {
    return repository.findAll().stream().map(NotificationResponse::from).toList();
  }
}
