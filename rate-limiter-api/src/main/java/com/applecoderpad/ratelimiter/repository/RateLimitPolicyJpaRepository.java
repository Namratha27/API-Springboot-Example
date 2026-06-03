package com.applecoderpad.ratelimiter.repository;

import com.applecoderpad.ratelimiter.model.RateLimitPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RateLimitPolicyJpaRepository extends JpaRepository<RateLimitPolicy, String> {}
