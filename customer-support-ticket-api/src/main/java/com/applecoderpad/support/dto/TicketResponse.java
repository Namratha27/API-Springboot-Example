package com.applecoderpad.support.dto;

import com.applecoderpad.support.model.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TicketResponse(
    UUID id,
    String customerId,
    String category,
    String subject,
    String description,
    TicketPriority priority,
    TicketStatus status,
    String assigneeId,
    List<TicketComment> comments,
    Instant createdAt,
    Instant updatedAt,
    Instant slaDueAt) {
  public static TicketResponse from(SupportTicket t) {
    return new TicketResponse(
        t.id(),
        t.customerId(),
        t.category(),
        t.subject(),
        t.description(),
        t.priority(),
        t.status(),
        t.assigneeId(),
        t.comments(),
        t.createdAt(),
        t.updatedAt(),
        t.slaDueAt());
  }
}
