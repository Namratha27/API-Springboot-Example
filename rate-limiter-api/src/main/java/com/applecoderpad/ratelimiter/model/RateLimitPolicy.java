package com.applecoderpad.ratelimiter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rate_limit_policies")
public class RateLimitPolicy {
  @Id private String clientId;

  @Column(nullable = false)
  private long capacity;

  @Column(nullable = false)
  private long refillPerSecond;

  protected RateLimitPolicy() {}

  public RateLimitPolicy(String clientId, long capacity, long refillPerSecond) {
    this.clientId = clientId;
    this.capacity = capacity;
    this.refillPerSecond = refillPerSecond;
  }

  public String clientId() {
    return clientId;
  }

  public long capacity() {
    return capacity;
  }

  public long refillPerSecond() {
    return refillPerSecond;
  }
}
