package com.applecoderpad.meetingrooms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.applecoderpad.meetingrooms.dto.BookingResponse;
import com.applecoderpad.meetingrooms.dto.CreateBookingRequest;
import com.applecoderpad.meetingrooms.exception.ConflictException;
import com.applecoderpad.meetingrooms.service.MeetingRoomService;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MeetingRoomSchedulerApiApplicationTests {
  @Autowired MeetingRoomService meetingRooms;

  @Test
  void booksRoom() {
    BookingResponse response =
        meetingRooms.book(
            new CreateBookingRequest(
                null,
                "alice",
                "Design review",
                4,
                Set.of("video"),
                Instant.now().plusSeconds(3600),
                Instant.now().plusSeconds(7200)));
    assertThat(response.id()).isNotNull();
  }

  @Test
  void rejectsOverlappingBookingForSameRoom() {
    Instant start = Instant.parse("2030-01-01T18:00:00Z");
    Instant end = Instant.parse("2030-01-01T19:00:00Z");
    meetingRooms.book(
        new CreateBookingRequest(
            "apple-park-1", "alice", "Roadmap", 4, Set.of("video"), start, end));

    assertThatThrownBy(
            () ->
                meetingRooms.book(
                    new CreateBookingRequest(
                        "apple-park-1",
                        "bob",
                        "Launch review",
                        4,
                        Set.of("video"),
                        start.plusSeconds(900),
                        end.plusSeconds(900))))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("already booked");
  }
}
