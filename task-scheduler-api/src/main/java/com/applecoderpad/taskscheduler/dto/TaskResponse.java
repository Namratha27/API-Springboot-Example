package com.applecoderpad.taskscheduler.dto;

import com.applecoderpad.taskscheduler.model.ScheduledTask;
import com.applecoderpad.taskscheduler.model.TaskStatus;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TaskResponse(
    UUID id,
    String name,
    Instant runAt,
    int priority,
    String callbackUrl,
    Map<String, Object> payload,
    Instant createdAt,
    TaskStatus status,
    int attempts,
    String lastError) {
  public static TaskResponse from(ScheduledTask task) {
    return new TaskResponse(
        task.id(),
        task.name(),
        task.runAt(),
        task.priority(),
        task.callbackUrl(),
        task.payload(),
        task.createdAt(),
        task.status(),
        task.attempts(),
        task.lastError());
  }
}
