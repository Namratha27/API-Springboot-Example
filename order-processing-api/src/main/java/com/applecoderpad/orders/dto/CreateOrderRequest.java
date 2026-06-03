package com.applecoderpad.orders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
    @NotBlank String customerId,
    @NotEmpty List<@Valid OrderLine> lines,
    @DecimalMin("0.01") BigDecimal totalAmount) {}
