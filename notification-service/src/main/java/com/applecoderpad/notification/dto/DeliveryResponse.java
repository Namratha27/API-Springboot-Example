package com.applecoderpad.notification.dto;

import com.applecoderpad.notification.model.Channel;
import com.applecoderpad.notification.model.DeliveryRecord;
import com.applecoderpad.notification.model.DeliveryStatus;
import java.util.UUID;

public record DeliveryResponse(
    UUID id,
    Channel channel,
    DeliveryStatus status,
    int attempts,
    String providerMessageId,
    String lastError) {
  public static DeliveryResponse from(DeliveryRecord delivery) {
    return new DeliveryResponse(
        delivery.id(),
        delivery.channel(),
        delivery.status(),
        delivery.attempts(),
        delivery.providerMessageId(),
        delivery.lastError());
  }
}
