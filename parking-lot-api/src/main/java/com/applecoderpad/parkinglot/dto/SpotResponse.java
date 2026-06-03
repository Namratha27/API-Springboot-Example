package com.applecoderpad.parkinglot.dto;

import com.applecoderpad.parkinglot.model.ParkingSpot;
import com.applecoderpad.parkinglot.model.SpotType;
import java.util.UUID;

public record SpotResponse(String id, int floor, SpotType type, boolean available, UUID ticketId) {
  public static SpotResponse from(ParkingSpot spot) {
    return new SpotResponse(
        spot.id(), spot.floor(), spot.type(), spot.available(), spot.ticketId());
  }
}
