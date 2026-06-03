package com.applecoderpad.orders.client;

import com.applecoderpad.orders.dto.*;
import com.applecoderpad.orders.model.CustomerOrder;
import java.net.URI;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class InventoryClient {
  private final RestClient restClient;
  private final boolean dryRun;
  private final String inventoryUrl;

  public InventoryClient(
      RestClient restClient,
      @Value("${orders.dry-run}") boolean dryRun,
      @Value("${orders.inventory-url}") String inventoryUrl) {
    this.restClient = restClient;
    this.dryRun = dryRun;
    this.inventoryUrl = inventoryUrl;
  }

  public InventoryReservation reserve(CustomerOrder order) {
    InventoryReservationRequest request =
        new InventoryReservationRequest(
            order.id().toString(),
            order.lines().stream()
                .map(l -> new InventoryReservationLine(l.sku(), l.quantity()))
                .toList());
    if (dryRun) return new InventoryReservation(UUID.randomUUID());
    return restClient
        .post()
        .uri(URI.create(inventoryUrl))
        .body(request)
        .retrieve()
        .body(InventoryReservation.class);
  }

  public void release(UUID reservationId) {
    if (dryRun) return;
    restClient
        .post()
        .uri(URI.create(inventoryUrl + "/" + reservationId + "/release"))
        .retrieve()
        .toBodilessEntity();
  }
}
