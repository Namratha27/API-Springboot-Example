package com.applecoderpad.parkinglot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "parking_spots")
public class ParkingSpot {
  @Id private String id;

  @Column(nullable = false)
  private int floor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SpotType type;

  private volatile UUID ticketId;

  protected ParkingSpot() {}

  public ParkingSpot(String id, int floor, SpotType type) {
    this.id = id;
    this.floor = floor;
    this.type = type;
  }

  public boolean accepts(VehicleType vehicleType, boolean ev) {
    return switch (vehicleType) {
      case MOTORCYCLE ->
          type == SpotType.BIKE || type == SpotType.COMPACT || type == SpotType.LARGE;
      case CAR -> type == SpotType.COMPACT || type == SpotType.LARGE || ev && type == SpotType.EV;
      case VAN -> type == SpotType.LARGE;
    };
  }

  public void occupy(UUID ticketId) {
    this.ticketId = ticketId;
  }

  public void release() {
    this.ticketId = null;
  }

  public boolean available() {
    return ticketId == null;
  }

  public int sortOrder() {
    return type.ordinal();
  }

  public String id() {
    return id;
  }

  public int floor() {
    return floor;
  }

  public SpotType type() {
    return type;
  }

  public UUID ticketId() {
    return ticketId;
  }
}
