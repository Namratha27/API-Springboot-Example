package com.applecoderpad.parkinglot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "parking_tickets")
public class ParkingTicket {
  @Id private UUID id;

  @Column(nullable = false)
  private String licensePlate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private VehicleType vehicleType;

  @Column(nullable = false)
  private String spotId;

  @Column(nullable = false)
  private Instant openedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private volatile TicketStatus status;

  private volatile Instant closedAt;
  private volatile BigDecimal fee;

  protected ParkingTicket() {}

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
