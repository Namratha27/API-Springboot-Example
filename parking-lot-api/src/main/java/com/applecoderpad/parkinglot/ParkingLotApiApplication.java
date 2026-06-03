package com.applecoderpad.parkinglot;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootApplication
public class ParkingLotApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ParkingLotApiApplication.class, args);
    }
}

@RestController
@RequestMapping("/parking")
class ParkingController {
    private final ParkingLotService parkingLot;

    ParkingController(ParkingLotService parkingLot) {
        this.parkingLot = parkingLot;
    }

    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    TicketResponse enter(@Valid @RequestBody ParkVehicleRequest request) {
        return parkingLot.park(request);
    }

    @PostMapping("/tickets/{ticketId}/exit")
    ExitResponse exit(@PathVariable UUID ticketId) {
        return parkingLot.exit(ticketId);
    }

    @GetMapping("/tickets/{ticketId}")
    TicketResponse ticket(@PathVariable UUID ticketId) {
        return parkingLot.ticket(ticketId);
    }

    @GetMapping("/availability")
    Map<SpotType, Long> availability() {
        return parkingLot.availability();
    }

    @GetMapping("/spots")
    Collection<SpotResponse> spots() {
        return parkingLot.spots();
    }
}

@Service
class ParkingLotService {
    private final ReentrantLock lock = new ReentrantLock();
    private final List<ParkingSpot> spots = new ArrayList<>();
    private final Map<UUID, ParkingTicket> tickets = new ConcurrentHashMap<>();

    ParkingLotService() {
        seedGarage();
    }

    TicketResponse park(ParkVehicleRequest request) {
        lock.lock();
        try {
            ParkingSpot spot = spots.stream()
                    .filter(ParkingSpot::available)
                    .filter(candidate -> candidate.accepts(request.vehicleType(), request.ev()))
                    .min(Comparator.comparing(ParkingSpot::floor).thenComparing(ParkingSpot::sortOrder))
                    .orElseThrow(() -> new ConflictException("no compatible spot available"));
            ParkingTicket ticket = ParkingTicket.open(
                    UUID.randomUUID(),
                    request.licensePlate(),
                    request.vehicleType(),
                    spot.id()
            );
            spot.occupy(ticket.id());
            tickets.put(ticket.id(), ticket);
            return TicketResponse.from(ticket, spot);
        } finally {
            lock.unlock();
        }
    }

    ExitResponse exit(UUID ticketId) {
        lock.lock();
        try {
            ParkingTicket ticket = getTicket(ticketId);
            if (ticket.status() == TicketStatus.CLOSED) {
                throw new ConflictException("ticket already closed");
            }
            ParkingSpot spot = getSpot(ticket.spotId());
            BigDecimal fee = calculateFee(ticket.openedAt(), Instant.now(), spot.type());
            ticket.close(fee);
            spot.release();
            return new ExitResponse(ticket.id(), ticket.licensePlate(), fee, ticket.openedAt(), ticket.closedAt());
        } finally {
            lock.unlock();
        }
    }

    TicketResponse ticket(UUID ticketId) {
        ParkingTicket ticket = getTicket(ticketId);
        return TicketResponse.from(ticket, getSpot(ticket.spotId()));
    }

    Map<SpotType, Long> availability() {
        lock.lock();
        try {
            Map<SpotType, Long> result = new EnumMap<>(SpotType.class);
            for (SpotType type : SpotType.values()) {
                result.put(type, spots.stream().filter(ParkingSpot::available).filter(spot -> spot.type() == type).count());
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    Collection<SpotResponse> spots() {
        return spots.stream().map(SpotResponse::from).toList();
    }

    private ParkingTicket getTicket(UUID ticketId) {
        ParkingTicket ticket = tickets.get(ticketId);
        if (ticket == null) {
            throw new NotFoundException("ticket not found: " + ticketId);
        }
        return ticket;
    }

    private ParkingSpot getSpot(String spotId) {
        return spots.stream()
                .filter(spot -> spot.id().equals(spotId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("spot not found: " + spotId));
    }

    private static BigDecimal calculateFee(Instant openedAt, Instant closedAt, SpotType type) {
        long hours = Math.max(1, Duration.between(openedAt, closedAt).toHours() + 1);
        BigDecimal hourly = switch (type) {
            case BIKE -> BigDecimal.valueOf(2);
            case COMPACT -> BigDecimal.valueOf(4);
            case LARGE -> BigDecimal.valueOf(6);
            case EV -> BigDecimal.valueOf(5);
        };
        return hourly.multiply(BigDecimal.valueOf(hours));
    }

    private void seedGarage() {
        for (int floor = 1; floor <= 2; floor++) {
            addSpots(floor, SpotType.BIKE, 5);
            addSpots(floor, SpotType.COMPACT, 10);
            addSpots(floor, SpotType.LARGE, 6);
            addSpots(floor, SpotType.EV, 4);
        }
    }

    private void addSpots(int floor, SpotType type, int count) {
        for (int i = 1; i <= count; i++) {
            spots.add(new ParkingSpot("F" + floor + "-" + type.name().charAt(0) + i, floor, type));
        }
    }
}

class ParkingSpot {
    private final String id;
    private final int floor;
    private final SpotType type;
    private volatile UUID ticketId;

    ParkingSpot(String id, int floor, SpotType type) {
        this.id = id;
        this.floor = floor;
        this.type = type;
    }

    boolean accepts(VehicleType vehicleType, boolean ev) {
        return switch (vehicleType) {
            case MOTORCYCLE -> type == SpotType.BIKE || type == SpotType.COMPACT || type == SpotType.LARGE;
            case CAR -> type == SpotType.COMPACT || type == SpotType.LARGE || ev && type == SpotType.EV;
            case VAN -> type == SpotType.LARGE;
        };
    }

    void occupy(UUID ticketId) {
        this.ticketId = ticketId;
    }

    void release() {
        this.ticketId = null;
    }

    boolean available() {
        return ticketId == null;
    }

    int sortOrder() {
        return type.ordinal();
    }

    String id() {
        return id;
    }

    int floor() {
        return floor;
    }

    SpotType type() {
        return type;
    }

    UUID ticketId() {
        return ticketId;
    }
}

class ParkingTicket {
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

    static ParkingTicket open(UUID id, String licensePlate, VehicleType vehicleType, String spotId) {
        return new ParkingTicket(id, licensePlate, vehicleType, spotId);
    }

    synchronized void close(BigDecimal fee) {
        this.fee = fee;
        this.closedAt = Instant.now();
        this.status = TicketStatus.CLOSED;
    }

    UUID id() {
        return id;
    }

    String licensePlate() {
        return licensePlate;
    }

    VehicleType vehicleType() {
        return vehicleType;
    }

    String spotId() {
        return spotId;
    }

    Instant openedAt() {
        return openedAt;
    }

    TicketStatus status() {
        return status;
    }

    Instant closedAt() {
        return closedAt;
    }

    BigDecimal fee() {
        return fee;
    }
}

record ParkVehicleRequest(@NotBlank String licensePlate, VehicleType vehicleType, boolean ev) {
}

record TicketResponse(UUID id,
                      String licensePlate,
                      VehicleType vehicleType,
                      String spotId,
                      int floor,
                      SpotType spotType,
                      TicketStatus status,
                      Instant openedAt,
                      Instant closedAt,
                      BigDecimal fee) {
    static TicketResponse from(ParkingTicket ticket, ParkingSpot spot) {
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
                ticket.fee()
        );
    }
}

record SpotResponse(String id, int floor, SpotType type, boolean available, UUID ticketId) {
    static SpotResponse from(ParkingSpot spot) {
        return new SpotResponse(spot.id(), spot.floor(), spot.type(), spot.available(), spot.ticketId());
    }
}

record ExitResponse(UUID ticketId, String licensePlate, BigDecimal fee, Instant openedAt, Instant closedAt) {
}

enum VehicleType {
    MOTORCYCLE, CAR, VAN
}

enum SpotType {
    BIKE, COMPACT, LARGE, EV
}

enum TicketStatus {
    OPEN, CLOSED
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException extends RuntimeException {
    NotFoundException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.CONFLICT)
class ConflictException extends RuntimeException {
    ConflictException(String message) {
        super(message);
    }
}
