package com.applecoderpad.parkinglot.service;

import com.applecoderpad.parkinglot.dto.ExitResponse;
import com.applecoderpad.parkinglot.dto.ParkVehicleRequest;
import com.applecoderpad.parkinglot.dto.SpotResponse;
import com.applecoderpad.parkinglot.dto.TicketResponse;
import com.applecoderpad.parkinglot.exception.ConflictException;
import com.applecoderpad.parkinglot.model.ParkingSpot;
import com.applecoderpad.parkinglot.model.ParkingTicket;
import com.applecoderpad.parkinglot.model.SpotType;
import com.applecoderpad.parkinglot.model.TicketStatus;
import com.applecoderpad.parkinglot.repository.ParkingLotRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;

@Service
public class ParkingLotService {
  private final ParkingLotRepository repository;
  private final ReentrantLock lock = new ReentrantLock();

  public ParkingLotService(ParkingLotRepository repository) {
    this.repository = repository;
  }

  public TicketResponse park(ParkVehicleRequest request) {
    lock.lock();
    try {
      ParkingSpot spot =
          repository.spots().stream()
              .filter(ParkingSpot::available)
              .filter(s -> s.accepts(request.vehicleType(), request.ev()))
              .min(Comparator.comparing(ParkingSpot::floor).thenComparing(ParkingSpot::sortOrder))
              .orElseThrow(() -> new ConflictException("no compatible spot available"));
      ParkingTicket ticket =
          ParkingTicket.open(
              UUID.randomUUID(), request.licensePlate(), request.vehicleType(), spot.id());
      spot.occupy(ticket.id());
      repository.save(ticket);
      return TicketResponse.from(ticket, spot);
    } finally {
      lock.unlock();
    }
  }

  public ExitResponse exit(UUID ticketId) {
    lock.lock();
    try {
      ParkingTicket ticket = repository.ticket(ticketId);
      if (ticket.status() == TicketStatus.CLOSED)
        throw new ConflictException("ticket already closed");
      ParkingSpot spot = repository.spot(ticket.spotId());
      BigDecimal fee = calculateFee(ticket.openedAt(), Instant.now(), spot.type());
      ticket.close(fee);
      spot.release();
      return new ExitResponse(
          ticket.id(), ticket.licensePlate(), fee, ticket.openedAt(), ticket.closedAt());
    } finally {
      lock.unlock();
    }
  }

  public TicketResponse ticket(UUID id) {
    ParkingTicket t = repository.ticket(id);
    return TicketResponse.from(t, repository.spot(t.spotId()));
  }

  public Map<SpotType, Long> availability() {
    Map<SpotType, Long> map = new EnumMap<>(SpotType.class);
    for (SpotType type : SpotType.values())
      map.put(
          type,
          repository.spots().stream()
              .filter(ParkingSpot::available)
              .filter(s -> s.type() == type)
              .count());
    return map;
  }

  public Collection<SpotResponse> spots() {
    return repository.spots().stream().map(SpotResponse::from).toList();
  }

  private static BigDecimal calculateFee(Instant openedAt, Instant closedAt, SpotType type) {
    long hours = Math.max(1, Duration.between(openedAt, closedAt).toHours() + 1);
    BigDecimal hourly =
        switch (type) {
          case BIKE -> BigDecimal.valueOf(2);
          case COMPACT -> BigDecimal.valueOf(4);
          case LARGE -> BigDecimal.valueOf(6);
          case EV -> BigDecimal.valueOf(5);
        };
    return hourly.multiply(BigDecimal.valueOf(hours));
  }
}
