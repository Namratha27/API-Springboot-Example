package com.applecoderpad.ratelimiter.filter;

import com.applecoderpad.ratelimiter.model.RateLimitDecision;
import com.applecoderpad.ratelimiter.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
  private final RateLimiterService rateLimiter;

  public RateLimitFilter(RateLimiterService rateLimiter) {
    this.rateLimiter = rateLimiter;
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
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"rate limit exceeded\"}");
      return;
    }
    filterChain.doFilter(request, response);
  }

  private static String clientId(HttpServletRequest request) {
    String apiKey = request.getHeader("X-API-Key");
    return apiKey == null || apiKey.isBlank() ? request.getRemoteAddr() : apiKey;
  }
}
