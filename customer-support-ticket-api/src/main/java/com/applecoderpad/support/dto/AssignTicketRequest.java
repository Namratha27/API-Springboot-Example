package com.applecoderpad.support.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignTicketRequest(@NotBlank String agentId) {}
