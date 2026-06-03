package com.applecoderpad.orders.controller;

import com.applecoderpad.orders.dto.CreateOrderRequest;
import com.applecoderpad.orders.dto.OrderResponse;
import com.applecoderpad.orders.service.OrderService;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
  private final OrderService orders;

  public OrderController(OrderService orders) {
    this.orders = orders;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OrderResponse create(
      @RequestHeader(value = "Idempotency-Key", required = false) String key,
      @Valid @RequestBody CreateOrderRequest request) {
    return orders.create(key, request);
  }

  @GetMapping("/{id}")
  public OrderResponse get(@PathVariable UUID id) {
    return orders.get(id);
  }

  @GetMapping
  public Collection<OrderResponse> list() {
    return orders.list();
  }

  @PostMapping("/{id}/cancel")
  public OrderResponse cancel(@PathVariable UUID id) {
    return orders.cancel(id);
  }
}
