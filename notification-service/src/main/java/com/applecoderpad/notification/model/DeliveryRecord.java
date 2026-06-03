package com.applecoderpad.notification.model;

import java.util.UUID;

public class DeliveryRecord {
  private final UUID id;
  private final UUID notificationId;
  private final Channel channel;
  private volatile DeliveryStatus status;
  private volatile int attempts;
  private volatile String providerMessageId;
  private volatile String lastError;

  private DeliveryRecord(UUID id, UUID notificationId, Channel channel) {
    this.id = id;
    this.notificationId = notificationId;
    this.channel = channel;
    this.status = DeliveryStatus.QUEUED;
  }

  public static DeliveryRecord create(UUID id, UUID notificationId, Channel channel) {
    return new DeliveryRecord(id, notificationId, channel);
  }

  public synchronized void markSending(int attempt) {
    attempts = attempt;
    status = DeliveryStatus.SENDING;
  }

  public synchronized void markSent(String providerMessageId) {
    this.providerMessageId = providerMessageId;
    status = DeliveryStatus.SENT;
    lastError = null;
  }

  public synchronized void markFailed(String error) {
    status = DeliveryStatus.FAILED;
    lastError = error;
  }

  public UUID id() {
    return id;
  }

  public UUID notificationId() {
    return notificationId;
  }

  public Channel channel() {
    return channel;
  }

  public DeliveryStatus status() {
    return status;
  }

  public int attempts() {
    return attempts;
  }

  public String providerMessageId() {
    return providerMessageId;
  }

  public String lastError() {
    return lastError;
  }
}
