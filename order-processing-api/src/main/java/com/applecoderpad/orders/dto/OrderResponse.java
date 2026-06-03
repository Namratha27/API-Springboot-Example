package com.applecoderpad.orders.dto;

import com.applecoderpad.orders.model.CustomerOrder;
import com.applecoderpad.orders.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    String customerId,
    List<OrderLine> lines,
    BigDecimal totalAmount,
    Instant createdAt,
    OrderStatus status,
    UUID inventoryReservationId,
    String paymentAuthorizationId,
    String failureReason) {
  public static OrderResponse from(CustomerOrder o) {
    return new OrderResponse(
        o.id(),
        o.customerId(),
        o.lines(),
        o.totalAmount(),
        o.createdAt(),
        o.status(),
        o.inventoryReservationId(),
        o.paymentAuthorizationId(),
        o.failureReason());
  }
}
