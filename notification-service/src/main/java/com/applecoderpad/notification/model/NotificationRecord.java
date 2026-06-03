package com.applecoderpad.notification.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationRecord {
  @Id private UUID id;

  @Column(nullable = false)
  private String recipient;

  @Column(nullable = false)
  private String subject;

  @Column(nullable = false, length = 4096)
  private String body;

  @Column(nullable = false)
  private Instant createdAt;

  @Transient private List<DeliveryRecord> deliveries = List.of();

  protected NotificationRecord() {}

  public NotificationRecord(
      UUID id,
      String recipient,
      String subject,
      String body,
      Instant createdAt,
      List<DeliveryRecord> deliveries) {
    this.id = id;
    this.recipient = recipient;
    this.subject = subject;
    this.body = body;
    this.createdAt = createdAt;
    this.deliveries = List.copyOf(deliveries);
  }

  public UUID id() {
    return id;
  }

  public String recipient() {
    return recipient;
  }

  public String subject() {
    return subject;
  }

  public String body() {
    return body;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public List<DeliveryRecord> deliveries() {
    return deliveries;
  }
}
