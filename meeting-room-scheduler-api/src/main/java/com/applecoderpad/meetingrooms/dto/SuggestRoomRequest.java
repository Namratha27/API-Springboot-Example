package com.applecoderpad.meetingrooms.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.Set;

public record SuggestRoomRequest(
    @Min(1) int attendeeCount, Set<String> features, @Future Instant start, @Future Instant end) {}
