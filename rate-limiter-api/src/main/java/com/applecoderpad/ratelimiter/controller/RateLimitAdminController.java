package com.applecoderpad.ratelimiter.controller;

import com.applecoderpad.ratelimiter.dto.PolicyResponse;
import com.applecoderpad.ratelimiter.dto.UpsertPolicyRequest;
import com.applecoderpad.ratelimiter.service.RateLimiterService;
import jakarta.validation.Valid;
import java.util.Collection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rate-limit")
public class RateLimitAdminController {
  private final RateLimiterService rateLimiter;

  public RateLimitAdminController(RateLimiterService rateLimiter) {
    this.rateLimiter = rateLimiter;
  }

  @PutMapping("/policies/{clientId}")
  public PolicyResponse configure(
      @PathVariable String clientId, @Valid @RequestBody UpsertPolicyRequest request) {
    return rateLimiter.configure(clientId, request);
  }

  @GetMapping("/policies")
  public Collection<PolicyResponse> policies() {
    return rateLimiter.policies();
  }
}
