package com.applecoderpad.notification.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public record DeliveryAttempt(UUID deliveryId, int attempt, Instant dueAt) implements Delayed {
  public static DeliveryAttempt now(UUID deliveryId, int attempt) {
    return new DeliveryAttempt(deliveryId, attempt, Instant.now());
  }

  public DeliveryAttempt next() {
    long delayMillis = (long) Math.pow(2, attempt) * 1000L;
    return new DeliveryAttempt(deliveryId, attempt + 1, Instant.now().plusMillis(delayMillis));
  }

  @Override
  public long getDelay(TimeUnit unit) {
    return unit.convert(Duration.between(Instant.now(), dueAt).toMillis(), TimeUnit.MILLISECONDS);
  }

  @Override
  public int compareTo(Delayed other) {
    return Comparator.comparing(DeliveryAttempt::dueAt).compare(this, (DeliveryAttempt) other);
  }
}
