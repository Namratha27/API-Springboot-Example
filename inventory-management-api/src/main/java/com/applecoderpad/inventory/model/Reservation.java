package com.applecoderpad.inventory.model;

import com.applecoderpad.inventory.dto.ReserveLine;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Reservation {
  private final UUID id;
  private final String orderId;
  private final List<ReserveLine> lines;
  private final Instant createdAt;
  private final Instant expiresAt;
  private volatile ReservationStatus status;

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
