package com.applecoderpad.inventory.model;

import com.applecoderpad.inventory.dto.ReserveLine;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "inventory_reservations")
public class Reservation {
  @Id private UUID id;

  @Column(nullable = false)
  private String orderId;

  @Transient private List<ReserveLine> lines = List.of();

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant expiresAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private volatile ReservationStatus status;

  protected Reservation() {}

  private Reservation(UUID id, String orderId, List<ReserveLine> lines, Instant expiresAt) {
    this.id = id;
    this.orderId = orderId;
    this.lines = List.copyOf(lines);
    this.createdAt = Instant.now();
    this.expiresAt = expiresAt;
    this.status = ReservationStatus.ACTIVE;
  }

  public static Reservation create(
      UUID id, String orderId, List<ReserveLine> lines, Instant expiresAt) {
    return new Reservation(id, orderId, lines, expiresAt);
  }

  public synchronized void release() {
    status = ReservationStatus.RELEASED;
  }

  public synchronized void commit() {
    status = ReservationStatus.COMMITTED;
  }

  public UUID id() {
    return id;
  }

  public String orderId() {
    return orderId;
  }

  public List<ReserveLine> lines() {
    return lines;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant expiresAt() {
    return expiresAt;
  }

  public ReservationStatus status() {
    return status;
  }
}
