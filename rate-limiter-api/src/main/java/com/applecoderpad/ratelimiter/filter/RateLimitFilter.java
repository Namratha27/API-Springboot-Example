package com.applecoderpad.ratelimiter.filter;

import com.applecoderpad.ratelimiter.model.RateLimitDecision;
import com.applecoderpad.ratelimiter.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
  private final RateLimiterService rateLimiter;
  private final ObjectMapper objectMapper;

  public RateLimitFilter(RateLimiterService rateLimiter, ObjectMapper objectMapper) {
    this.rateLimiter = rateLimiter;
    this.objectMapper = objectMapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/actuator") || path.startsWith("/rate-limit");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String clientId = clientId(request);
    RateLimitDecision decision = rateLimiter.check(clientId);
    response.setHeader("X-RateLimit-Client", clientId);
    response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.remainingTokens()));
    response.setHeader(
        "X-RateLimit-Retry-After-Millis", String.valueOf(decision.retryAfter().toMillis()));
    if (!decision.allowed()) {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
      response.setHeader(
          HttpHeaders.RETRY_AFTER, String.valueOf(Math.max(1L, decision.retryAfter().toSeconds())));
      objectMapper.writeValue(response.getWriter(), problem(request, decision));
      return;
    }
    filterChain.doFilter(request, response);
  }

  private static String clientId(HttpServletRequest request) {
    String apiKey = request.getHeader("X-API-Key");
    return apiKey == null || apiKey.isBlank() ? request.getRemoteAddr() : apiKey;
  }

  private static Map<String, Object> problem(
      HttpServletRequest request, RateLimitDecision decision) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("type", "about:blank");
    body.put("title", HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase());
    body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
    body.put("detail", "rate limit exceeded");
    body.put("instance", request.getRequestURI());
    body.put("code", HttpStatus.TOO_MANY_REQUESTS.name());
    body.put("retryAfterMillis", decision.retryAfter().toMillis());
    return body;
  }
}
