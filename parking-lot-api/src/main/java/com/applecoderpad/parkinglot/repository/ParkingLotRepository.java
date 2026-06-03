package com.applecoderpad.parkinglot.repository;

import com.applecoderpad.parkinglot.exception.NotFoundException;
import com.applecoderpad.parkinglot.model.ParkingSpot;
import com.applecoderpad.parkinglot.model.ParkingTicket;
import com.applecoderpad.parkinglot.model.SpotType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class ParkingLotRepository {
  private final List<ParkingSpot> spots = new ArrayList<>();
  private final Map<UUID, ParkingTicket> tickets = new ConcurrentHashMap<>();

  public ParkingLotRepository() {
    for (int floor = 1; floor <= 2; floor++) {
      add(floor, SpotType.BIKE, 5);
      add(floor, SpotType.COMPACT, 10);
      add(floor, SpotType.LARGE, 6);
      add(floor, SpotType.EV, 4);
    }
  }

  private void add(int floor, SpotType type, int count) {
    for (int i = 1; i <= count; i++)
      spots.add(new ParkingSpot("F" + floor + "-" + type.name().charAt(0) + i, floor, type));
  }

  public List<ParkingSpot> spots() {
    return spots;
  }

  public Collection<ParkingTicket> tickets() {
    return tickets.values();
  }

  public void save(ParkingTicket ticket) {
    tickets.put(ticket.id(), ticket);
  }

  public ParkingTicket ticket(UUID id) {
    ParkingTicket t = tickets.get(id);
    if (t == null) throw new NotFoundException("ticket not found: " + id);
    return t;
  }

  public ParkingSpot spot(String id) {
    return spots.stream()
        .filter(s -> s.id().equals(id))
        .findFirst()
        .orElseThrow(() -> new NotFoundException("spot not found: " + id));
  }
}
