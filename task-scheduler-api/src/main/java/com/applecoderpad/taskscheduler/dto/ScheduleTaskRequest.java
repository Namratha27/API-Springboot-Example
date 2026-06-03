package com.applecoderpad.taskscheduler.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;

public record ScheduleTaskRequest(
    @NotBlank String name,
    @FutureOrPresent Instant runAt,
    @Min(0) @Max(10) int priority,
    String callbackUrl,
    Map<String, Object> payload) {}
