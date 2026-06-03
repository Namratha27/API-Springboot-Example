package com.applecoderpad.ratelimiter;

import static org.assertj.core.api.Assertions.assertThat;

import com.applecoderpad.ratelimiter.dto.UpsertPolicyRequest;
import com.applecoderpad.ratelimiter.filter.RateLimitFilter;
import com.applecoderpad.ratelimiter.service.RateLimiterService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@SpringBootTest
class RateLimiterApiApplicationTests {
  @Autowired private RateLimiterService rateLimiter;
  @Autowired private RateLimitFilter filter;

  @Test
  void deniesWhenBucketIsEmpty() {
    rateLimiter.configure("client-a", new UpsertPolicyRequest(1, 1));
    assertThat(rateLimiter.check("client-a").allowed()).isTrue();
    assertThat(rateLimiter.check("client-a").allowed()).isFalse();
  }

  @Test
  void returnsProblemDetailsWhenRequestIsLimited() throws Exception {
    rateLimiter.configure("limited-client-http", new UpsertPolicyRequest(1, 1));

    MockHttpServletResponse allowed = doFilter("limited-client-http");
    assertThat(allowed.getStatus()).isEqualTo(HttpStatus.OK.value());

    MockHttpServletResponse limited = doFilter("limited-client-http");
    assertThat(limited.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    assertThat(limited.getHeader("Retry-After")).isNotBlank();
    assertThat(limited.getContentAsString()).contains("\"code\":\"TOO_MANY_REQUESTS\"");
  }

  private MockHttpServletResponse doFilter(String clientId) throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/demo");
    request.addHeader("X-API-Key", clientId);
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilter(
        request,
        response,
        (servletRequest, servletResponse) -> {
          ((HttpServletResponse) servletResponse).setStatus(HttpStatus.OK.value());
        });
    return response;
  }
}
