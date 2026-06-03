package com.applecoderpad.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReserveLine(@NotBlank String sku, @Min(1) int quantity) {}
