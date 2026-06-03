package com.applecoderpad.inventory.repository;

import com.applecoderpad.inventory.exception.ConflictException;
import com.applecoderpad.inventory.exception.NotFoundException;
import com.applecoderpad.inventory.model.Product;
import com.applecoderpad.inventory.model.Reservation;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryRepository {
  private final Map<String, Product> products = new ConcurrentHashMap<>();
  private final Map<UUID, Reservation> reservations = new ConcurrentHashMap<>();
  private final Map<String, UUID> idempotencyKeys = new ConcurrentHashMap<>();

  public void insert(Product product) {
    if (products.putIfAbsent(product.sku(), product) != null)
      throw new ConflictException("product already exists: " + product.sku());
  }

  public Product product(String sku) {
    Product p = products.get(sku);
    if (p == null) throw new NotFoundException("product not found: " + sku);
    return p;
  }

  public Collection<Product> products() {
    return products.values();
  }

  public void save(Reservation r) {
    reservations.put(r.id(), r);
  }

  public Reservation reservation(UUID id) {
    Reservation r = reservations.get(id);
    if (r == null) throw new NotFoundException("reservation not found: " + id);
    return r;
  }

  public Collection<Reservation> reservations() {
    return reservations.values();
  }

  public boolean hasKey(String key) {
    return key != null && idempotencyKeys.containsKey(key);
  }

  public UUID idFor(String key) {
    return idempotencyKeys.get(key);
  }

  public void saveKey(String key, UUID id) {
    idempotencyKeys.putIfAbsent(key, id);
  }
}
