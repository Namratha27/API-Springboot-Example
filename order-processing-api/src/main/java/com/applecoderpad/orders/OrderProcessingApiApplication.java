package com.applecoderpad.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class OrderProcessingApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderProcessingApiApplication.class, args);
    }

    @Bean
    RestClient restClient() {
        return RestClient.create();
    }
}

@RestController
@RequestMapping("/orders")
class OrderController {
    private final OrderService orders;

    OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OrderResponse create(@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                         @Valid @RequestBody CreateOrderRequest request) {
        return orders.create(idempotencyKey, request);
    }

    @GetMapping("/{orderId}")
    OrderResponse get(@PathVariable UUID orderId) {
        return orders.get(orderId);
    }

    @GetMapping
    Collection<OrderResponse> list() {
        return orders.list();
    }

    @PostMapping("/{orderId}/cancel")
    OrderResponse cancel(@PathVariable UUID orderId) {
        return orders.cancel(orderId);
    }
}

@Service
class OrderService {
    private final OrderRepository repository;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;

    OrderService(OrderRepository repository, InventoryClient inventoryClient, PaymentClient paymentClient) {
        this.repository = repository;
        this.inventoryClient = inventoryClient;
        this.paymentClient = paymentClient;
    }

    OrderResponse create(String idempotencyKey, CreateOrderRequest request) {
        if (idempotencyKey != null && repository.hasIdempotencyKey(idempotencyKey)) {
            return OrderResponse.from(repository.get(repository.orderIdFor(idempotencyKey)));
        }
        CustomerOrder order = CustomerOrder.create(UUID.randomUUID(), request.customerId(), request.lines(), request.totalAmount());
        repository.save(order);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            repository.saveIdempotencyKey(idempotencyKey, order.id());
        }
        try {
            InventoryReservation reservation = inventoryClient.reserve(order);
            order.markInventoryReserved(reservation.reservationId());
            PaymentAuthorization authorization = paymentClient.authorize(order);
            order.markAccepted(authorization.authorizationId());
        } catch (RuntimeException ex) {
            order.markFailed(ex.getMessage());
            if (order.inventoryReservationId() != null) {
                inventoryClient.release(order.inventoryReservationId());
            }
        }
        return OrderResponse.from(order);
    }

    OrderResponse get(UUID orderId) {
        return OrderResponse.from(repository.get(orderId));
    }

    Collection<OrderResponse> list() {
        return repository.findAll().stream().map(OrderResponse::from).toList();
    }

    OrderResponse cancel(UUID orderId) {
        CustomerOrder order = repository.get(orderId);
        synchronized (order) {
            if (order.status() == OrderStatus.CANCELED || order.status() == OrderStatus.FAILED) {
                return OrderResponse.from(order);
            }
            if (order.status() == OrderStatus.COMPLETED) {
                throw new ConflictException("completed orders cannot be canceled");
            }
            if (order.inventoryReservationId() != null) {
                inventoryClient.release(order.inventoryReservationId());
            }
            if (order.paymentAuthorizationId() != null) {
                paymentClient.voidAuthorization(order.paymentAuthorizationId());
            }
            order.cancel();
            return OrderResponse.from(order);
        }
    }
}

@Service
class InventoryClient {
    private final RestClient restClient;
    private final boolean dryRun;
    private final String inventoryUrl;

    InventoryClient(RestClient restClient,
                    @Value("${orders.dry-run}") boolean dryRun,
                    @Value("${orders.inventory-url}") String inventoryUrl) {
        this.restClient = restClient;
        this.dryRun = dryRun;
        this.inventoryUrl = inventoryUrl;
    }

    InventoryReservation reserve(CustomerOrder order) {
        InventoryReservationRequest request = new InventoryReservationRequest(
                order.id().toString(),
                order.lines().stream().map(line -> new InventoryReservationLine(line.sku(), line.quantity())).toList()
        );
        if (dryRun) {
            return new InventoryReservation(UUID.randomUUID());
        }
        return restClient.post()
                .uri(URI.create(inventoryUrl))
                .body(request)
                .retrieve()
                .body(InventoryReservation.class);
    }

    void release(UUID reservationId) {
        if (dryRun) {
            return;
        }
        restClient.post()
                .uri(URI.create(inventoryUrl + "/" + reservationId + "/release"))
                .retrieve()
                .toBodilessEntity();
    }
}

@Service
class PaymentClient {
    private final RestClient restClient;
    private final boolean dryRun;
    private final String paymentUrl;

    PaymentClient(RestClient restClient,
                  @Value("${orders.dry-run}") boolean dryRun,
                  @Value("${orders.payment-url}") String paymentUrl) {
        this.restClient = restClient;
        this.dryRun = dryRun;
        this.paymentUrl = paymentUrl;
    }

    PaymentAuthorization authorize(CustomerOrder order) {
        PaymentRequest request = new PaymentRequest(order.id(), order.customerId(), order.totalAmount());
        if (dryRun) {
            return new PaymentAuthorization("auth-" + order.id());
        }
        return restClient.post()
                .uri(URI.create(paymentUrl))
                .body(request)
                .retrieve()
                .body(PaymentAuthorization.class);
    }

    void voidAuthorization(String authorizationId) {
        if (dryRun) {
            return;
        }
        restClient.post()
                .uri(URI.create(paymentUrl + "/" + authorizationId + "/void"))
                .retrieve()
                .toBodilessEntity();
    }
}

@Repository
class OrderRepository {
    private final Map<UUID, CustomerOrder> orders = new ConcurrentHashMap<>();
    private final Map<String, UUID> idempotencyKeys = new ConcurrentHashMap<>();

    void save(CustomerOrder order) {
        orders.put(order.id(), order);
    }

    CustomerOrder get(UUID id) {
        CustomerOrder order = orders.get(id);
        if (order == null) {
            throw new NotFoundException("order not found: " + id);
        }
        return order;
    }

    Collection<CustomerOrder> findAll() {
        return orders.values();
    }

    boolean hasIdempotencyKey(String key) {
        return key != null && idempotencyKeys.containsKey(key);
    }

    UUID orderIdFor(String key) {
        return idempotencyKeys.get(key);
    }

    void saveIdempotencyKey(String key, UUID orderId) {
        idempotencyKeys.putIfAbsent(key, orderId);
    }
}

class CustomerOrder {
    private final UUID id;
    private final String customerId;
    private final List<OrderLine> lines;
    private final BigDecimal totalAmount;
    private final Instant createdAt;
    private volatile OrderStatus status;
    private volatile UUID inventoryReservationId;
    private volatile String paymentAuthorizationId;
    private volatile String failureReason;

    private CustomerOrder(UUID id, String customerId, List<OrderLine> lines, BigDecimal totalAmount) {
        this.id = id;
        this.customerId = customerId;
        this.lines = List.copyOf(lines);
        this.totalAmount = totalAmount;
        this.createdAt = Instant.now();
        this.status = OrderStatus.PENDING;
    }

    static CustomerOrder create(UUID id, String customerId, List<OrderLine> lines, BigDecimal totalAmount) {
        return new CustomerOrder(id, customerId, lines, totalAmount);
    }

    synchronized void markInventoryReserved(UUID reservationId) {
        this.inventoryReservationId = reservationId;
        this.status = OrderStatus.INVENTORY_RESERVED;
    }

    synchronized void markAccepted(String authorizationId) {
        this.paymentAuthorizationId = authorizationId;
        this.status = OrderStatus.ACCEPTED;
    }

    synchronized void markFailed(String reason) {
        this.failureReason = reason;
        this.status = OrderStatus.FAILED;
    }

    synchronized void cancel() {
        this.status = OrderStatus.CANCELED;
    }

    UUID id() {
        return id;
    }

    String customerId() {
        return customerId;
    }

    List<OrderLine> lines() {
        return lines;
    }

    BigDecimal totalAmount() {
        return totalAmount;
    }

    Instant createdAt() {
        return createdAt;
    }

    OrderStatus status() {
        return status;
    }

    UUID inventoryReservationId() {
        return inventoryReservationId;
    }

    String paymentAuthorizationId() {
        return paymentAuthorizationId;
    }

    String failureReason() {
        return failureReason;
    }
}

record CreateOrderRequest(@NotBlank String customerId,
                          @NotEmpty List<@Valid OrderLine> lines,
                          @DecimalMin("0.01") BigDecimal totalAmount) {
}

record OrderLine(@NotBlank String sku, @Min(1) int quantity) {
}

record OrderResponse(UUID id,
                     String customerId,
                     List<OrderLine> lines,
                     BigDecimal totalAmount,
                     Instant createdAt,
                     OrderStatus status,
                     UUID inventoryReservationId,
                     String paymentAuthorizationId,
                     String failureReason) {
    static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(
                order.id(),
                order.customerId(),
                order.lines(),
                order.totalAmount(),
                order.createdAt(),
                order.status(),
                order.inventoryReservationId(),
                order.paymentAuthorizationId(),
                order.failureReason()
        );
    }
}

record InventoryReservationRequest(String orderId, List<InventoryReservationLine> lines) {
}

record InventoryReservationLine(String sku, int quantity) {
}

record InventoryReservation(UUID reservationId) {
}

record PaymentRequest(UUID orderId, String customerId, BigDecimal amount) {
}

record PaymentAuthorization(String authorizationId) {
}

enum OrderStatus {
    PENDING, INVENTORY_RESERVED, ACCEPTED, COMPLETED, CANCELED, FAILED
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
