package com.applecoderpad.ratelimiter.model;

import java.time.Duration;

public record RateLimitDecision(boolean allowed, long remainingTokens, Duration retryAfter) {}
