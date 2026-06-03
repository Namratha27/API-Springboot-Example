package com.applecoderpad.ratelimiter;

import static org.assertj.core.api.Assertions.assertThat;

import com.applecoderpad.ratelimiter.dto.UpsertPolicyRequest;
import com.applecoderpad.ratelimiter.service.RateLimiterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RateLimiterApiApplicationTests {
  @Autowired private RateLimiterService rateLimiter;

  @Test
  void deniesWhenBucketIsEmpty() {
    rateLimiter.configure("client-a", new UpsertPolicyRequest(1, 1));
    assertThat(rateLimiter.check("client-a").allowed()).isTrue();
    assertThat(rateLimiter.check("client-a").allowed()).isFalse();
  }
}
