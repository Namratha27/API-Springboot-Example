package com.applecoderpad.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateProductRequest(
    @NotBlank String sku,
    @NotBlank String name,
    @Min(0) int onHand,
    @Min(0) int reorderThreshold) {}
