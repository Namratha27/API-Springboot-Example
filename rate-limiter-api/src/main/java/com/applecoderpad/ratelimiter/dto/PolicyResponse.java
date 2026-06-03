package com.applecoderpad.ratelimiter.dto;

import com.applecoderpad.ratelimiter.model.RateLimitPolicy;

public record PolicyResponse(String clientId, long capacity, long refillPerSecond) {
  public static PolicyResponse from(RateLimitPolicy policy) {
    return new PolicyResponse(policy.clientId(), policy.capacity(), policy.refillPerSecond());
  }
}
