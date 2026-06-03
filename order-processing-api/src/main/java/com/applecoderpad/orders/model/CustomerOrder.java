package com.applecoderpad.orders.model;

import com.applecoderpad.orders.dto.OrderLine;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
  @Id private UUID id;

  @Column(nullable = false)
  private String customerId;

  @Transient private List<OrderLine> lines = List.of();

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal totalAmount;

  @Column(nullable = false)
  private Instant createdAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private volatile OrderStatus status;

  private volatile UUID inventoryReservationId;

  @Column(length = 512)
  private volatile String paymentAuthorizationId;

  @Column(length = 1024)
  private volatile String failureReason;

  protected CustomerOrder() {}

  private CustomerOrder(UUID id, String customerId, List<OrderLine> lines, BigDecimal totalAmount) {
    this.id = id;
    this.customerId = customerId;
    this.lines = List.copyOf(lines);
    this.totalAmount = totalAmount;
    this.createdAt = Instant.now();
    this.status = OrderStatus.PENDING;
  }

  public static CustomerOrder create(
      UUID id, String customerId, List<OrderLine> lines, BigDecimal totalAmount) {
    return new CustomerOrder(id, customerId, lines, totalAmount);
  }

  public synchronized void markInventoryReserved(UUID id) {
    inventoryReservationId = id;
    status = OrderStatus.INVENTORY_RESERVED;
  }

  public synchronized void markAccepted(String auth) {
    paymentAuthorizationId = auth;
    status = OrderStatus.ACCEPTED;
  }

  public synchronized void markFailed(String reason) {
    failureReason = reason;
    status = OrderStatus.FAILED;
  }

  public synchronized void cancel() {
    status = OrderStatus.CANCELED;
  }

  public UUID id() {
    return id;
  }

  public String customerId() {
    return customerId;
  }

  public List<OrderLine> lines() {
    return lines;
  }

  public BigDecimal totalAmount() {
    return totalAmount;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public OrderStatus status() {
    return status;
  }

  public UUID inventoryReservationId() {
    return inventoryReservationId;
  }

  public String paymentAuthorizationId() {
    return paymentAuthorizationId;
  }

  public String failureReason() {
    return failureReason;
  }
}
