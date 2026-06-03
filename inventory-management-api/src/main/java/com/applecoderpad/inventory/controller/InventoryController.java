package com.applecoderpad.inventory.controller;

import com.applecoderpad.inventory.dto.*;
import com.applecoderpad.inventory.service.InventoryService;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class InventoryController {
  private final InventoryService inventory;

  public InventoryController(InventoryService inventory) {
    this.inventory = inventory;
  }

  @PostMapping("/products")
  @ResponseStatus(HttpStatus.CREATED)
  public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
    return inventory.create(request);
  }

  @GetMapping("/products")
  public Collection<ProductResponse> products() {
    return inventory.products();
  }

  @GetMapping("/products/{sku}")
  public ProductResponse product(@PathVariable String sku) {
    return inventory.product(sku);
  }

  @PatchMapping("/products/{sku}/stock")
  public ProductResponse adjust(
      @PathVariable String sku, @Valid @RequestBody AdjustStockRequest request) {
    return inventory.adjust(sku, request);
  }

  @PostMapping("/reservations")
  @ResponseStatus(HttpStatus.CREATED)
  public ReservationResponse reserve(
      @RequestHeader(value = "Idempotency-Key", required = false) String key,
      @Valid @RequestBody ReserveInventoryRequest request) {
    return inventory.reserve(key, request);
  }

  @PostMapping("/reservations/{id}/release")
  public ReservationResponse release(@PathVariable UUID id) {
    return inventory.release(id);
  }

  @PostMapping("/reservations/{id}/commit")
  public ReservationResponse commit(@PathVariable UUID id) {
    return inventory.commit(id);
  }

  @GetMapping("/reservations/{id}")
  public ReservationResponse reservation(@PathVariable UUID id) {
    return inventory.reservation(id);
  }
}
