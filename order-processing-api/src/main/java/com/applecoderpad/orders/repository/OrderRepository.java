package com.applecoderpad.orders.repository;

import com.applecoderpad.orders.exception.NotFoundException;
import com.applecoderpad.orders.model.CustomerOrder;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {
  private final Map<UUID, CustomerOrder> orders = new ConcurrentHashMap<>();
  private final Map<String, UUID> idempotencyKeys = new ConcurrentHashMap<>();

  public void save(CustomerOrder o) {
    orders.put(o.id(), o);
  }

  public CustomerOrder get(UUID id) {
    CustomerOrder o = orders.get(id);
    if (o == null) throw new NotFoundException("order not found: " + id);
    return o;
  }

  public Collection<CustomerOrder> findAll() {
    return orders.values();
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
