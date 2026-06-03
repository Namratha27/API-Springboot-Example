package com.applecoderpad.parkinglot.model;

import java.util.UUID;

public class ParkingSpot {
  private final String id;
  private final int floor;
  private final SpotType type;
  private volatile UUID ticketId;

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
