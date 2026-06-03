package com.applecoderpad.parkinglot.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ExitResponse(
    UUID ticketId, String licensePlate, BigDecimal fee, Instant openedAt, Instant closedAt) {}
