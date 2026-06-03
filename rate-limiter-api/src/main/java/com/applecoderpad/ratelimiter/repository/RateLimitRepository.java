package com.applecoderpad.ratelimiter.repository;

import com.applecoderpad.ratelimiter.model.RateLimitPolicy;
import com.applecoderpad.ratelimiter.model.TokenBucket;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class RateLimitRepository {
  private final Map<String, RateLimitPolicy> policies = new ConcurrentHashMap<>();
  private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

  public RateLimitPolicy policyFor(String clientId, RateLimitPolicy defaultPolicy) {
    return policies.getOrDefault(clientId, defaultPolicy);
  }

  public TokenBucket bucketFor(String clientId, RateLimitPolicy policy) {
    return buckets.computeIfAbsent(clientId, ignored -> new TokenBucket(policy.capacity()));
  }

  public void savePolicy(RateLimitPolicy policy) {
    policies.put(policy.clientId(), policy);
    buckets.put(policy.clientId(), new TokenBucket(policy.capacity()));
  }

  public Collection<RateLimitPolicy> policies() {
    return policies.values();
  }
}
