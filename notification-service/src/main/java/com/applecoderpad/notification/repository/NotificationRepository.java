package com.applecoderpad.notification.repository;

import com.applecoderpad.notification.exception.NotFoundException;
import com.applecoderpad.notification.model.DeliveryRecord;
import com.applecoderpad.notification.model.NotificationRecord;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepository {
  private final Map<UUID, NotificationRecord> notifications = new ConcurrentHashMap<>();
  private final Map<UUID, DeliveryRecord> deliveries = new ConcurrentHashMap<>();

  public void save(NotificationRecord notification) {
    notifications.put(notification.id(), notification);
    notification.deliveries().forEach(delivery -> deliveries.put(delivery.id(), delivery));
  }

  public NotificationRecord get(UUID id) {
    NotificationRecord notification = notifications.get(id);
    if (notification == null) throw new NotFoundException("notification not found: " + id);
    return notification;
  }

  public DeliveryRecord getDelivery(UUID id) {
    DeliveryRecord delivery = deliveries.get(id);
    if (delivery == null) throw new NotFoundException("delivery not found: " + id);
    return delivery;
  }

  public Collection<NotificationRecord> findAll() {
    return notifications.values();
  }
}
