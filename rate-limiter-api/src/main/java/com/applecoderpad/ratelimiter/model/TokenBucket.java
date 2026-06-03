package com.applecoderpad.ratelimiter.model;

import java.time.Duration;
import java.time.Instant;

public class TokenBucket {
  private double tokens;
  private Instant lastRefill;

  public TokenBucket(long initialTokens) {
    this.tokens = initialTokens;
    this.lastRefill = Instant.now();
  }

  public synchronized RateLimitDecision tryConsume(RateLimitPolicy policy, Instant now) {
    refill(policy, now);
    if (tokens >= 1.0d) {
      tokens -= 1.0d;
      return new RateLimitDecision(true, (long) tokens, Duration.ZERO);
    }
    long millis = Math.max(1L, (long) Math.ceil(1000.0d / policy.refillPerSecond()));
    return new RateLimitDecision(false, 0, Duration.ofMillis(millis));
  }

  private void refill(RateLimitPolicy policy, Instant now) {
    long elapsedMillis = Duration.between(lastRefill, now).toMillis();
    if (elapsedMillis <= 0) return;
    double refill = elapsedMillis / 1000.0d * policy.refillPerSecond();
    tokens = Math.min(policy.capacity(), tokens + refill);
    lastRefill = now;
  }
}
