package com.applecoderpad.ratelimiter.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoApiController {
  @GetMapping("/api/demo")
  public Map<String, String> demo() {
    return Map.of("status", "allowed", "message", "request passed the token bucket");
  }
}
