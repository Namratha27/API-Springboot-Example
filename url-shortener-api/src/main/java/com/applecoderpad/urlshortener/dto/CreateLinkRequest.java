package com.applecoderpad.urlshortener.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;

public record CreateLinkRequest(
    @NotBlank @Pattern(regexp = "https?://.+", message = "must start with http:// or https://")
        String originalUrl,
    String customAlias,
    @Future Instant expiresAt) {}
