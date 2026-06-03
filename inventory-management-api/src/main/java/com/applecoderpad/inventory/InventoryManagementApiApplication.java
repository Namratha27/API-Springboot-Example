package com.applecoderpad.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@EnableScheduling
public class InventoryManagementApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryManagementApiApplication.class, args);
    }
}

@RestController
class InventoryController {
    private final InventoryService inventory;

    InventoryController(InventoryService inventory) {
        this.inventory = inventory;
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
        return inventory.create(request);
    }

    @GetMapping("/products")
    Collection<ProductResponse> products() {
        return inventory.products();
    }

    @GetMapping("/products/{sku}")
    ProductResponse product(@PathVariable String sku) {
        return inventory.product(sku);
    }

    @PatchMapping("/products/{sku}/stock")
    ProductResponse adjust(@PathVariable String sku, @Valid @RequestBody AdjustStockRequest request) {
        return inventory.adjust(sku, request);
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    ReservationResponse reserve(@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                @Valid @RequestBody ReserveInventoryRequest request) {
        return inventory.reserve(idempotencyKey, request);
    }

    @PostMapping("/reservations/{reservationId}/release")
    ReservationResponse release(@PathVariable UUID reservationId) {
        return inventory.release(reservationId);
    }

    @PostMapping("/reservations/{reservationId}/commit")
    ReservationResponse commit(@PathVariable UUID reservationId) {
        return inventory.commit(reservationId);
    }

    @GetMapping("/reservations/{reservationId}")
    ReservationResponse reservation(@PathVariable UUID reservationId) {
        return inventory.reservation(reservationId);
    }
}

@Service
class InventoryService {
    private final InventoryRepository repository;
    private final long reservationTtlSeconds;

    InventoryService(InventoryRepository repository,
                     @Value("${inventory.reservation-ttl-seconds}") long reservationTtlSeconds) {
        this.repository = repository;
        this.reservationTtlSeconds = reservationTtlSeconds;
    }

    ProductResponse create(CreateProductRequest request) {
        Product product = Product.create(request.sku(), request.name(), request.onHand(), request.reorderThreshold());
        repository.insert(product);
        return ProductResponse.from(product);
    }

    Collection<ProductResponse> products() {
        return repository.products().stream().map(ProductResponse::from).toList();
    }

    ProductResponse product(String sku) {
        return ProductResponse.from(repository.getProduct(sku));
    }

    ProductResponse adjust(String sku, AdjustStockRequest request) {
        Product product = repository.getProduct(sku);
        product.adjust(request.delta(), request.reason());
        return ProductResponse.from(product);
    }

    ReservationResponse reserve(String idempotencyKey, ReserveInventoryRequest request) {
        if (idempotencyKey != null && repository.hasIdempotencyKey(idempotencyKey)) {
            return ReservationResponse.from(repository.getReservation(repository.reservationIdFor(idempotencyKey)));
        }
        List<Product> products = request.lines().stream()
                .map(line -> repository.getProduct(line.sku()))
                .toList();
        synchronized (repository) {
            for (ReserveLine line : request.lines()) {
                Product product = repository.getProduct(line.sku());
                if (product.available() < line.quantity()) {
                    throw new ConflictException("insufficient stock for " + line.sku());
                }
            }
            Reservation reservation = Reservation.create(
                    UUID.randomUUID(),
                    request.orderId(),
                    request.lines(),
                    Instant.now().plusSeconds(reservationTtlSeconds)
            );
            for (ReserveLine line : request.lines()) {
                repository.getProduct(line.sku()).reserve(line.quantity());
            }
            repository.saveReservation(reservation);
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                repository.saveIdempotencyKey(idempotencyKey, reservation.id());
            }
            products.forEach(Product::touch);
            return ReservationResponse.from(reservation);
        }
    }

    ReservationResponse release(UUID reservationId) {
        Reservation reservation = repository.getReservation(reservationId);
        synchronized (repository) {
            if (reservation.status() != ReservationStatus.ACTIVE) {
                return ReservationResponse.from(reservation);
            }
            reservation.release();
            reservation.lines().forEach(line -> repository.getProduct(line.sku()).release(line.quantity()));
            return ReservationResponse.from(reservation);
        }
    }

    ReservationResponse commit(UUID reservationId) {
        Reservation reservation = repository.getReservation(reservationId);
        synchronized (repository) {
            if (reservation.status() != ReservationStatus.ACTIVE) {
                throw new ConflictException("reservation is not active");
            }
            reservation.commit();
            return ReservationResponse.from(reservation);
        }
    }

    ReservationResponse reservation(UUID reservationId) {
        return ReservationResponse.from(repository.getReservation(reservationId));
    }

    @Scheduled(fixedDelay = 60_000)
    void expireReservations() {
        repository.reservations().stream()
                .filter(reservation -> reservation.status() == ReservationStatus.ACTIVE)
                .filter(reservation -> reservation.expiresAt().isBefore(Instant.now()))
                .forEach(reservation -> release(reservation.id()));
    }
}

@Repository
class InventoryRepository {
    private final Map<String, Product> products = new ConcurrentHashMap<>();
    private final Map<UUID, Reservation> reservations = new ConcurrentHashMap<>();
    private final Map<String, UUID> idempotencyKeys = new ConcurrentHashMap<>();

    void insert(Product product) {
        Product previous = products.putIfAbsent(product.sku(), product);
        if (previous != null) {
            throw new ConflictException("product already exists: " + product.sku());
        }
    }

    Product getProduct(String sku) {
        Product product = products.get(sku);
        if (product == null) {
            throw new NotFoundException("product not found: " + sku);
        }
        return product;
    }

    Collection<Product> products() {
        return products.values();
    }

    void saveReservation(Reservation reservation) {
        reservations.put(reservation.id(), reservation);
    }

    Reservation getReservation(UUID id) {
        Reservation reservation = reservations.get(id);
        if (reservation == null) {
            throw new NotFoundException("reservation not found: " + id);
        }
        return reservation;
    }

    Collection<Reservation> reservations() {
        return reservations.values();
    }

    boolean hasIdempotencyKey(String idempotencyKey) {
        return idempotencyKey != null && idempotencyKeys.containsKey(idempotencyKey);
    }

    UUID reservationIdFor(String idempotencyKey) {
        return idempotencyKeys.get(idempotencyKey);
    }

    void saveIdempotencyKey(String idempotencyKey, UUID reservationId) {
        idempotencyKeys.putIfAbsent(idempotencyKey, reservationId);
    }
}

class Product {
    private final String sku;
    private final String name;
    private final int reorderThreshold;
    private int onHand;
    private int reserved;
    private long version;
    private Instant updatedAt;

    private Product(String sku, String name, int onHand, int reorderThreshold) {
        this.sku = sku;
        this.name = name;
        this.onHand = onHand;
        this.reorderThreshold = reorderThreshold;
        this.updatedAt = Instant.now();
    }

    static Product create(String sku, String name, int onHand, int reorderThreshold) {
        return new Product(sku, name, onHand, reorderThreshold);
    }

    synchronized void adjust(int delta, String reason) {
        if (onHand + delta < reserved) {
            throw new ConflictException("adjustment would put on-hand below reserved stock");
        }
        onHand += delta;
        touch();
    }

    synchronized void reserve(int quantity) {
        if (available() < quantity) {
            throw new ConflictException("insufficient available stock");
        }
        reserved += quantity;
        touch();
    }

    synchronized void release(int quantity) {
        reserved = Math.max(0, reserved - quantity);
        touch();
    }

    synchronized void touch() {
        version++;
        updatedAt = Instant.now();
    }

    synchronized int available() {
        return onHand - reserved;
    }

    String sku() {
        return sku;
    }

    String name() {
        return name;
    }

    synchronized int onHand() {
        return onHand;
    }

    synchronized int reserved() {
        return reserved;
    }

    int reorderThreshold() {
        return reorderThreshold;
    }

    synchronized long version() {
        return version;
    }

    synchronized Instant updatedAt() {
        return updatedAt;
    }
}

class Reservation {
    private final UUID id;
    private final String orderId;
    private final List<ReserveLine> lines;
    private final Instant createdAt;
    private final Instant expiresAt;
    private volatile ReservationStatus status;

    private Reservation(UUID id, String orderId, List<ReserveLine> lines, Instant expiresAt) {
        this.id = id;
        this.orderId = orderId;
        this.lines = List.copyOf(lines);
        this.createdAt = Instant.now();
        this.expiresAt = expiresAt;
        this.status = ReservationStatus.ACTIVE;
    }

    static Reservation create(UUID id, String orderId, List<ReserveLine> lines, Instant expiresAt) {
        return new Reservation(id, orderId, lines, expiresAt);
    }

    synchronized void release() {
        status = ReservationStatus.RELEASED;
    }

    synchronized void commit() {
        status = ReservationStatus.COMMITTED;
    }

    UUID id() {
        return id;
    }

    String orderId() {
        return orderId;
    }

    List<ReserveLine> lines() {
        return lines;
    }

    Instant createdAt() {
        return createdAt;
    }

    Instant expiresAt() {
        return expiresAt;
    }

    ReservationStatus status() {
        return status;
    }
}

record CreateProductRequest(@NotBlank String sku,
                            @NotBlank String name,
                            @Min(0) int onHand,
                            @Min(0) int reorderThreshold) {
}

record AdjustStockRequest(int delta, String reason) {
}

record ReserveInventoryRequest(@NotBlank String orderId, @NotEmpty List<ReserveLine> lines) {
}

record ReserveLine(@NotBlank String sku, @Min(1) int quantity) {
}

record ProductResponse(String sku,
                       String name,
                       int onHand,
                       int reserved,
                       int available,
                       int reorderThreshold,
                       boolean reorderRecommended,
                       long version,
                       Instant updatedAt) {
    static ProductResponse from(Product product) {
        return new ProductResponse(
                product.sku(),
                product.name(),
                product.onHand(),
                product.reserved(),
                product.available(),
                product.reorderThreshold(),
                product.available() <= product.reorderThreshold(),
                product.version(),
                product.updatedAt()
        );
    }
}

record ReservationResponse(UUID id,
                           String orderId,
                           List<ReserveLine> lines,
                           ReservationStatus status,
                           Instant createdAt,
                           Instant expiresAt) {
    static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.id(),
                reservation.orderId(),
                reservation.lines(),
                reservation.status(),
                reservation.createdAt(),
                reservation.expiresAt()
        );
    }
}

enum ReservationStatus {
    ACTIVE, RELEASED, COMMITTED
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
