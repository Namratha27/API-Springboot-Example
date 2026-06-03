package com.applecoderpad.meetingrooms.controller;

import com.applecoderpad.meetingrooms.dto.BookingResponse;
import com.applecoderpad.meetingrooms.dto.CreateBookingRequest;
import com.applecoderpad.meetingrooms.dto.RoomResponse;
import com.applecoderpad.meetingrooms.dto.SuggestRoomRequest;
import com.applecoderpad.meetingrooms.service.MeetingRoomService;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meeting-rooms")
public class MeetingRoomController {
  private final MeetingRoomService meetingRooms;

  public MeetingRoomController(MeetingRoomService meetingRooms) {
    this.meetingRooms = meetingRooms;
  }

  @GetMapping
  public Collection<RoomResponse> rooms() {
    return meetingRooms.rooms();
  }

  @PostMapping("/bookings")
  @ResponseStatus(HttpStatus.CREATED)
  public BookingResponse book(@Valid @RequestBody CreateBookingRequest request) {
    return meetingRooms.book(request);
  }

  @GetMapping("/bookings")
  public Collection<BookingResponse> bookings(@RequestParam(required = false) String roomId) {
    return meetingRooms.bookings(roomId);
  }

  @DeleteMapping("/bookings/{bookingId}")
  public BookingResponse cancel(@PathVariable UUID bookingId) {
    return meetingRooms.cancel(bookingId);
  }

  @PostMapping("/suggestions")
  public Collection<RoomResponse> suggest(@Valid @RequestBody SuggestRoomRequest request) {
    return meetingRooms.suggest(request);
  }
}
