package com.applecoderpad.ratelimiter.service;

import com.applecoderpad.ratelimiter.dto.PolicyResponse;
import com.applecoderpad.ratelimiter.dto.UpsertPolicyRequest;
import com.applecoderpad.ratelimiter.model.RateLimitDecision;
import com.applecoderpad.ratelimiter.model.RateLimitPolicy;
import com.applecoderpad.ratelimiter.repository.RateLimitRepository;
import java.time.Instant;
import java.util.Collection;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {
  private final RateLimitRepository repository;
  private final RateLimitPolicy defaultPolicy = new RateLimitPolicy("default", 10, 5);

  public RateLimiterService(RateLimitRepository repository) {
    this.repository = repository;
  }

  public RateLimitDecision check(String clientId) {
    RateLimitPolicy policy = repository.policyFor(clientId, defaultPolicy);
    return repository.bucketFor(clientId, policy).tryConsume(policy, Instant.now());
  }

  public PolicyResponse configure(String clientId, UpsertPolicyRequest request) {
    RateLimitPolicy policy =
        new RateLimitPolicy(clientId, request.capacity(), request.refillPerSecond());
    repository.savePolicy(policy);
    return PolicyResponse.from(policy);
  }

  public Collection<PolicyResponse> policies() {
    return repository.policies().stream().map(PolicyResponse::from).toList();
  }
}
