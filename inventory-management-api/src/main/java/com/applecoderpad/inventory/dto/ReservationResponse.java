package com.applecoderpad.inventory.dto;

import com.applecoderpad.inventory.model.Reservation;
import com.applecoderpad.inventory.model.ReservationStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationResponse(
    UUID id,
    String orderId,
    List<ReserveLine> lines,
    ReservationStatus status,
    Instant createdAt,
    Instant expiresAt) {
  public static ReservationResponse from(Reservation r) {
    return new ReservationResponse(
        r.id(), r.orderId(), r.lines(), r.status(), r.createdAt(), r.expiresAt());
  }
}
