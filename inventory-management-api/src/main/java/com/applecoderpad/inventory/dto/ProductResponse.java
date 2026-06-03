package com.applecoderpad.inventory.dto;

import com.applecoderpad.inventory.model.Product;
import java.time.Instant;

public record ProductResponse(
    String sku,
    String name,
    int onHand,
    int reserved,
    int available,
    int reorderThreshold,
    boolean reorderRecommended,
    long version,
    Instant updatedAt) {
  public static ProductResponse from(Product p) {
    return new ProductResponse(
        p.sku(),
        p.name(),
        p.onHand(),
        p.reserved(),
        p.available(),
        p.reorderThreshold(),
        p.available() <= p.reorderThreshold(),
        p.version(),
        p.updatedAt());
  }
}
