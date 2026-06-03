package com.applecoderpad.parkinglot.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ParkingTicket {
  private final UUID id;
  private final String licensePlate;
  private final VehicleType vehicleType;
  private final String spotId;
  private final Instant openedAt;
  private volatile TicketStatus status;
  private volatile Instant closedAt;
  private volatile BigDecimal fee;

  private ParkingTicket(UUID id, String licensePlate, VehicleType vehicleType, String spotId) {
    this.id = id;
    this.licensePlate = licensePlate;
    this.vehicleType = vehicleType;
    this.spotId = spotId;
    this.openedAt = Instant.now();
    this.status = TicketStatus.OPEN;
  }

  public static ParkingTicket open(
      UUID id, String licensePlate, VehicleType vehicleType, String spotId) {
    return new ParkingTicket(id, licensePlate, vehicleType, spotId);
  }

  public synchronized void close(BigDecimal fee) {
    this.fee = fee;
    this.closedAt = Instant.now();
    this.status = TicketStatus.CLOSED;
  }

  public UUID id() {
    return id;
  }

  public String licensePlate() {
    return licensePlate;
  }

  public VehicleType vehicleType() {
    return vehicleType;
  }

  public String spotId() {
    return spotId;
  }

  public Instant openedAt() {
    return openedAt;
  }

  public TicketStatus status() {
    return status;
  }

  public Instant closedAt() {
    return closedAt;
  }

  public BigDecimal fee() {
    return fee;
  }
}
