package com.applecoderpad.notification.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "notification_deliveries")
public class DeliveryRecord {
  @Id private UUID id;

  @Column(nullable = false)
  private UUID notificationId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Channel channel;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private volatile DeliveryStatus status;

  private volatile int attempts;

  @Column(length = 512)
  private volatile String providerMessageId;

  @Column(length = 1024)
  private volatile String lastError;

  protected DeliveryRecord() {}

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
