package com.applecoderpad.support.dto;

import com.applecoderpad.support.model.TicketPriority;
import jakarta.validation.constraints.NotBlank;

public record CreateTicketRequest(
    @NotBlank String customerId,
    @NotBlank String category,
    @NotBlank String subject,
    @NotBlank String description,
    TicketPriority priority) {}
