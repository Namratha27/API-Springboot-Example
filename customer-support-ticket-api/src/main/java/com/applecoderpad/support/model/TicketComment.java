package com.applecoderpad.support.model;

import java.time.Instant;
import java.util.UUID;

public record TicketComment(
    UUID id, String author, String body, Instant createdAt, boolean internal) {}
