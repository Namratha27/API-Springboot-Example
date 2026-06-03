package com.applecoderpad.parkinglot.dto;

import com.applecoderpad.parkinglot.model.ParkingSpot;
import com.applecoderpad.parkinglot.model.ParkingTicket;
import com.applecoderpad.parkinglot.model.SpotType;
import com.applecoderpad.parkinglot.model.TicketStatus;
import com.applecoderpad.parkinglot.model.VehicleType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
    UUID id,
    String licensePlate,
    VehicleType vehicleType,
    String spotId,
    int floor,
    SpotType spotType,
    TicketStatus status,
    Instant openedAt,
    Instant closedAt,
    BigDecimal fee) {
  public static TicketResponse from(ParkingTicket ticket, ParkingSpot spot) {
    return new TicketResponse(
        ticket.id(),
        ticket.licensePlate(),
        ticket.vehicleType(),
        spot.id(),
        spot.floor(),
        spot.type(),
        ticket.status(),
        ticket.openedAt(),
        ticket.closedAt(),
        ticket.fee());
  }
}
