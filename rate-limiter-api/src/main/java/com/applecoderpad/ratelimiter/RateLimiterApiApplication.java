package com.applecoderpad.ratelimiter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class RateLimiterApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(RateLimiterApiApplication.class, args);
    }
}

@RestController
@RequestMapping("/rate-limit")
class RateLimitAdminController {
    private final RateLimiterService rateLimiter;

    RateLimitAdminController(RateLimiterService rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @PutMapping("/policies/{clientId}")
    PolicyResponse configure(@PathVariable String clientId, @Valid @RequestBody UpsertPolicyRequest request) {
        return rateLimiter.configure(clientId, request);
    }

    @GetMapping("/policies")
    Collection<PolicyResponse> policies() {
        return rateLimiter.policies();
    }
}

@RestController
class DemoApiController {
    @GetMapping("/api/demo")
    Map<String, String> demo() {
        return Map.of("status", "allowed", "message", "request passed the token bucket");
    }
}

@Service
class RateLimiterService {
    private final Map<String, RateLimitPolicy> policies = new ConcurrentHashMap<>();
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final RateLimitPolicy defaultPolicy = new RateLimitPolicy("default", 10, 5);

    RateLimitDecision check(String clientId) {
        RateLimitPolicy policy = policies.getOrDefault(clientId, defaultPolicy);
        TokenBucket bucket = buckets.computeIfAbsent(clientId, ignored -> new TokenBucket(policy.capacity()));
        return bucket.tryConsume(policy, Instant.now());
    }

    PolicyResponse configure(String clientId, UpsertPolicyRequest request) {
        RateLimitPolicy policy = new RateLimitPolicy(clientId, request.capacity(), request.refillPerSecond());
        policies.put(clientId, policy);
        buckets.put(clientId, new TokenBucket(policy.capacity()));
        return PolicyResponse.from(policy);
    }

    Collection<PolicyResponse> policies() {
        return policies.values().stream().map(PolicyResponse::from).toList();
    }
}

@Component
class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimiterService rateLimiter;

    RateLimitFilter(RateLimiterService rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/rate-limit");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientId = clientId(request);
        RateLimitDecision decision = rateLimiter.check(clientId);
        response.setHeader("X-RateLimit-Client", clientId);
        response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.remainingTokens()));
        response.setHeader("X-RateLimit-Retry-After-Millis", String.valueOf(decision.retryAfter().toMillis()));
        if (!decision.allowed()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"error":"rate limit exceeded"}
                    """);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static String clientId(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey;
        }
        return request.getRemoteAddr();
    }
}

class TokenBucket {
    private double tokens;
    private Instant lastRefill;

    TokenBucket(long initialTokens) {
        this.tokens = initialTokens;
        this.lastRefill = Instant.now();
    }

    synchronized RateLimitDecision tryConsume(RateLimitPolicy policy, Instant now) {
        refill(policy, now);
        if (tokens >= 1.0d) {
            tokens -= 1.0d;
            return new RateLimitDecision(true, (long) tokens, Duration.ZERO);
        }
        long millis = Math.max(1L, (long) Math.ceil(1_000.0d / policy.refillPerSecond()));
        return new RateLimitDecision(false, 0, Duration.ofMillis(millis));
    }

    private void refill(RateLimitPolicy policy, Instant now) {
        long elapsedMillis = Duration.between(lastRefill, now).toMillis();
        if (elapsedMillis <= 0) {
            return;
        }
        double refill = elapsedMillis / 1_000.0d * policy.refillPerSecond();
        tokens = Math.min(policy.capacity(), tokens + refill);
        lastRefill = now;
    }
}

record UpsertPolicyRequest(@Min(1) @Max(1_000_000) long capacity,
                           @Min(1) @Max(1_000_000) long refillPerSecond) {
}

record RateLimitPolicy(String clientId, long capacity, long refillPerSecond) {
}

record RateLimitDecision(boolean allowed, long remainingTokens, Duration retryAfter) {
}

record PolicyResponse(String clientId, long capacity, long refillPerSecond) {
    static PolicyResponse from(RateLimitPolicy policy) {
        return new PolicyResponse(policy.clientId(), policy.capacity(), policy.refillPerSecond());
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequestException extends RuntimeException {
    BadRequestException(String message) {
        super(message);
    }
}
