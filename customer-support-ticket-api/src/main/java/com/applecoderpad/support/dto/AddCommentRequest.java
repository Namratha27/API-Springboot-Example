package com.applecoderpad.support.dto;

import jakarta.validation.constraints.NotBlank;

public record AddCommentRequest(@NotBlank String author, @NotBlank String body, boolean internal) {}
