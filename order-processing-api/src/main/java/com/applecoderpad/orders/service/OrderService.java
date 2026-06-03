package com.applecoderpad.orders.service;

import com.applecoderpad.orders.client.InventoryClient;
import com.applecoderpad.orders.client.PaymentClient;
import com.applecoderpad.orders.dto.*;
import com.applecoderpad.orders.exception.ConflictException;
import com.applecoderpad.orders.model.CustomerOrder;
import com.applecoderpad.orders.model.OrderStatus;
import com.applecoderpad.orders.repository.OrderRepository;
import java.util.Collection;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
  private final OrderRepository repo;
  private final InventoryClient inventory;
  private final PaymentClient payment;

  public OrderService(OrderRepository repo, InventoryClient inventory, PaymentClient payment) {
    this.repo = repo;
    this.inventory = inventory;
    this.payment = payment;
  }

  public OrderResponse create(String key, CreateOrderRequest r) {
    if (repo.hasKey(key)) return OrderResponse.from(repo.get(repo.idFor(key)));
    CustomerOrder order =
        CustomerOrder.create(UUID.randomUUID(), r.customerId(), r.lines(), r.totalAmount());
    repo.save(order);
    if (key != null && !key.isBlank()) repo.saveKey(key, order.id());
    try {
      InventoryReservation reservation = inventory.reserve(order);
      order.markInventoryReserved(reservation.reservationId());
      PaymentAuthorization auth = payment.authorize(order);
      order.markAccepted(auth.authorizationId());
    } catch (RuntimeException ex) {
      order.markFailed(ex.getMessage());
      if (order.inventoryReservationId() != null) inventory.release(order.inventoryReservationId());
    }
    return OrderResponse.from(order);
  }

  public OrderResponse get(UUID id) {
    return OrderResponse.from(repo.get(id));
  }

  public Collection<OrderResponse> list() {
    return repo.findAll().stream().map(OrderResponse::from).toList();
  }

  public OrderResponse cancel(UUID id) {
    CustomerOrder order = repo.get(id);
    synchronized (order) {
      if (order.status() == OrderStatus.COMPLETED)
        throw new ConflictException("completed orders cannot be canceled");
      if (order.inventoryReservationId() != null) inventory.release(order.inventoryReservationId());
      if (order.paymentAuthorizationId() != null)
        payment.voidAuthorization(order.paymentAuthorizationId());
      order.cancel();
      return OrderResponse.from(order);
    }
  }
}
