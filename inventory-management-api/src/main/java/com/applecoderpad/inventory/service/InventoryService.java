package com.applecoderpad.inventory.service;

import com.applecoderpad.inventory.dto.*;
import com.applecoderpad.inventory.exception.ConflictException;
import com.applecoderpad.inventory.model.*;
import com.applecoderpad.inventory.repository.InventoryRepository;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
  private final InventoryRepository repo;
  private final long ttlSeconds;

  public InventoryService(
      InventoryRepository repo, @Value("${inventory.reservation-ttl-seconds}") long ttlSeconds) {
    this.repo = repo;
    this.ttlSeconds = ttlSeconds;
  }

  public ProductResponse create(CreateProductRequest r) {
    Product p = Product.create(r.sku(), r.name(), r.onHand(), r.reorderThreshold());
    repo.insert(p);
    return ProductResponse.from(p);
  }

  public Collection<ProductResponse> products() {
    return repo.products().stream().map(ProductResponse::from).toList();
  }

  public ProductResponse product(String sku) {
    return ProductResponse.from(repo.product(sku));
  }

  public ProductResponse adjust(String sku, AdjustStockRequest r) {
    Product p = repo.product(sku);
    p.adjust(r.delta());
    return ProductResponse.from(p);
  }

  public ReservationResponse reserve(String key, ReserveInventoryRequest r) {
    if (repo.hasKey(key)) return ReservationResponse.from(repo.reservation(repo.idFor(key)));
    synchronized (repo) {
      for (ReserveLine line : r.lines())
        if (repo.product(line.sku()).available() < line.quantity())
          throw new ConflictException("insufficient stock for " + line.sku());
      Reservation reservation =
          Reservation.create(
              UUID.randomUUID(), r.orderId(), r.lines(), Instant.now().plusSeconds(ttlSeconds));
      for (ReserveLine line : r.lines()) repo.product(line.sku()).reserve(line.quantity());
      repo.save(reservation);
      if (key != null && !key.isBlank()) repo.saveKey(key, reservation.id());
      return ReservationResponse.from(reservation);
    }
  }

  public ReservationResponse release(UUID id) {
    Reservation r = repo.reservation(id);
    synchronized (repo) {
      if (r.status() == ReservationStatus.ACTIVE) {
        r.release();
        r.lines().forEach(line -> repo.product(line.sku()).release(line.quantity()));
      }
      return ReservationResponse.from(r);
    }
  }

  public ReservationResponse commit(UUID id) {
    Reservation r = repo.reservation(id);
    synchronized (repo) {
      if (r.status() != ReservationStatus.ACTIVE)
        throw new ConflictException("reservation is not active");
      r.commit();
      return ReservationResponse.from(r);
    }
  }

  public ReservationResponse reservation(UUID id) {
    return ReservationResponse.from(repo.reservation(id));
  }

  @Scheduled(fixedDelay = 60000)
  public void expire() {
    repo.reservations().stream()
        .filter(r -> r.status() == ReservationStatus.ACTIVE)
        .filter(r -> r.expiresAt().isBefore(Instant.now()))
        .forEach(r -> release(r.id()));
  }
}
