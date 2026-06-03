package com.applecoderpad.orders.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record OrderLine(@NotBlank String sku, @Min(1) int quantity) {}
