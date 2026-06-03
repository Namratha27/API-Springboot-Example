package com.applecoderpad.taskscheduler.model;

import com.applecoderpad.taskscheduler.exception.ConflictException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "scheduled_tasks")
public class ScheduledTask {
  @Id private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private Instant runAt;

  @Column(nullable = false)
  private int priority;

  @Column(length = 2048)
  private String callbackUrl;

  @Transient private Map<String, Object> payload = Map.of();

  @Column(nullable = false)
  private Instant createdAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private volatile TaskStatus status;

  private volatile int attempts;

  @Column(length = 1024)
  private volatile String lastError;

  protected ScheduledTask() {}

  private ScheduledTask(
      UUID id,
      String name,
      Instant runAt,
      int priority,
      String callbackUrl,
      Map<String, Object> payload) {
    this.id = id;
    this.name = name;
    this.runAt = runAt;
    this.priority = priority;
    this.callbackUrl = callbackUrl;
    this.payload = payload == null ? Map.of() : Map.copyOf(payload);
    this.createdAt = Instant.now();
    this.status = TaskStatus.SCHEDULED;
  }

  public static ScheduledTask create(
      UUID id,
      String name,
      Instant runAt,
      int priority,
      String callbackUrl,
      Map<String, Object> payload) {
    return new ScheduledTask(id, name, runAt, priority, callbackUrl, payload);
  }

  public synchronized boolean isRunnable() {
    return status == TaskStatus.SCHEDULED || status == TaskStatus.FAILED;
  }

  public synchronized void markRunning() {
    attempts++;
    status = TaskStatus.RUNNING;
  }

  public synchronized void markSucceeded() {
    status = TaskStatus.SUCCEEDED;
    lastError = null;
  }

  public synchronized void markFailed(String error) {
    status = TaskStatus.FAILED;
    lastError = error;
  }

  public synchronized void cancel() {
    if (status == TaskStatus.SUCCEEDED || status == TaskStatus.RUNNING)
      throw new ConflictException("cannot cancel task in status " + status);
    status = TaskStatus.CANCELED;
  }

  public UUID id() {
    return id;
  }

  public String name() {
    return name;
  }

  public Instant runAt() {
    return runAt;
  }

  public int priority() {
    return priority;
  }

  public String callbackUrl() {
    return callbackUrl;
  }

  public Map<String, Object> payload() {
    return payload;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public TaskStatus status() {
    return status;
  }

  public int attempts() {
    return attempts;
  }

  public String lastError() {
    return lastError;
  }
}
