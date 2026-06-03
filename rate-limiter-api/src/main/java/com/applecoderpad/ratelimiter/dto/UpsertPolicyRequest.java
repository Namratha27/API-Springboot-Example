package com.applecoderpad.ratelimiter.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpsertPolicyRequest(
    @Min(1) @Max(1000000) long capacity, @Min(1) @Max(1000000) long refillPerSecond) {}
