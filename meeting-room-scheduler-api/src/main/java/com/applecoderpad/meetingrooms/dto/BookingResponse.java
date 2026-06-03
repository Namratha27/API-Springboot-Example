package com.applecoderpad.meetingrooms.dto;

import com.applecoderpad.meetingrooms.model.Booking;
import com.applecoderpad.meetingrooms.model.BookingStatus;
import com.applecoderpad.meetingrooms.model.MeetingRoom;
import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
    UUID id,
    String roomId,
    String roomName,
    String organizer,
    String title,
    int attendeeCount,
    Instant start,
    Instant end,
    BookingStatus status) {
  public static BookingResponse from(Booking b, MeetingRoom r) {
    return new BookingResponse(
        b.id(),
        b.roomId(),
        r.name(),
        b.organizer(),
        b.title(),
        b.attendeeCount(),
        b.start(),
        b.end(),
        b.status());
  }
}
