package com.applecoderpad.ratelimiter.model;

public record RateLimitPolicy(String clientId, long capacity, long refillPerSecond) {}
