package com.applecoderpad.notification.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record NotificationRecord(
    UUID id,
    String recipient,
    String subject,
    String body,
    Instant createdAt,
    List<DeliveryRecord> deliveries) {}
