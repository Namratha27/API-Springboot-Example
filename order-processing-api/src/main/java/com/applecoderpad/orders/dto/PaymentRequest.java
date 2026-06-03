package com.applecoderpad.orders.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(UUID orderId, String customerId, BigDecimal amount) {}
