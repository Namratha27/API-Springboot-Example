package com.applecoderpad.notification.controller;

import com.applecoderpad.notification.dto.NotificationResponse;
import com.applecoderpad.notification.dto.SendNotificationRequest;
import com.applecoderpad.notification.service.NotificationService;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
  private final NotificationService notifications;

  public NotificationController(NotificationService notifications) {
    this.notifications = notifications;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public NotificationResponse send(@Valid @RequestBody SendNotificationRequest request) {
    return notifications.enqueue(request);
  }

  @GetMapping("/{id}")
  public NotificationResponse get(@PathVariable UUID id) {
    return notifications.get(id);
  }

  @GetMapping
  public Collection<NotificationResponse> list() {
    return notifications.list();
  }
}
