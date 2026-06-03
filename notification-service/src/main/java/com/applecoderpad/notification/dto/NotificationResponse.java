package com.applecoderpad.notification.dto;

import com.applecoderpad.notification.model.NotificationRecord;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    String recipient,
    String subject,
    String body,
    Instant createdAt,
    List<DeliveryResponse> deliveries) {
  public static NotificationResponse from(NotificationRecord notification) {
    return new NotificationResponse(
        notification.id(),
        notification.recipient(),
        notification.subject(),
        notification.body(),
        notification.createdAt(),
        notification.deliveries().stream().map(DeliveryResponse::from).toList());
  }
}
