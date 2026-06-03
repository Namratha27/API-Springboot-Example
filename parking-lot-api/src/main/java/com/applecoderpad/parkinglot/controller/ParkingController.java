package com.applecoderpad.parkinglot.controller;

import com.applecoderpad.parkinglot.dto.ExitResponse;
import com.applecoderpad.parkinglot.dto.ParkVehicleRequest;
import com.applecoderpad.parkinglot.dto.SpotResponse;
import com.applecoderpad.parkinglot.dto.TicketResponse;
import com.applecoderpad.parkinglot.model.SpotType;
import com.applecoderpad.parkinglot.service.ParkingLotService;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/parking")
public class ParkingController {
  private final ParkingLotService parkingLot;

  public ParkingController(ParkingLotService parkingLot) {
    this.parkingLot = parkingLot;
  }

  @PostMapping("/tickets")
  @ResponseStatus(HttpStatus.CREATED)
  public TicketResponse enter(@Valid @RequestBody ParkVehicleRequest request) {
    return parkingLot.park(request);
  }

  @PostMapping("/tickets/{ticketId}/exit")
  public ExitResponse exit(@PathVariable UUID ticketId) {
    return parkingLot.exit(ticketId);
  }

  @GetMapping("/tickets/{ticketId}")
  public TicketResponse ticket(@PathVariable UUID ticketId) {
    return parkingLot.ticket(ticketId);
  }

  @GetMapping("/availability")
  public Map<SpotType, Long> availability() {
    return parkingLot.availability();
  }

  @GetMapping("/spots")
  public Collection<SpotResponse> spots() {
    return parkingLot.spots();
  }
}
