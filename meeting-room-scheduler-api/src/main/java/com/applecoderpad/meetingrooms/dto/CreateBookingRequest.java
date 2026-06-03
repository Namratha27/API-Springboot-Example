package com.applecoderpad.meetingrooms.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Set;

public record CreateBookingRequest(
    String roomId,
    @NotBlank String organizer,
    @NotBlank String title,
    @Min(1) int attendeeCount,
    Set<String> features,
    @Future Instant start,
    @Future Instant end) {}
