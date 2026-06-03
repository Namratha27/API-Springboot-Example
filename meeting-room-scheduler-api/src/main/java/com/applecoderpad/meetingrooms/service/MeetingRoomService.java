package com.applecoderpad.meetingrooms.service;

import com.applecoderpad.meetingrooms.dto.*;
import com.applecoderpad.meetingrooms.exception.BadRequestException;
import com.applecoderpad.meetingrooms.exception.ConflictException;
import com.applecoderpad.meetingrooms.model.*;
import com.applecoderpad.meetingrooms.repository.MeetingRoomRepository;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;

@Service
public class MeetingRoomService {
  private final MeetingRoomRepository repo;
  private final ReentrantLock lock = new ReentrantLock();

  public MeetingRoomService(MeetingRoomRepository repo) {
    this.repo = repo;
  }

  public Collection<RoomResponse> rooms() {
    return repo.rooms().stream().map(RoomResponse::from).toList();
  }

  public BookingResponse book(CreateBookingRequest r) {
    validate(r.start(), r.end());
    lock.lock();
    try {
      MeetingRoom room =
          (r.roomId() == null || r.roomId().isBlank())
              ? firstAvailable(r.attendeeCount(), r.features(), r.start(), r.end())
              : repo.room(r.roomId());
      if (room.capacity() < r.attendeeCount())
        throw new ConflictException("room capacity is too small");
      if (!room.features().containsAll(safe(r.features())))
        throw new ConflictException("room does not satisfy requested features");
      if (!available(room.id(), r.start(), r.end()))
        throw new ConflictException("room already booked");
      Booking booking =
          Booking.create(
              UUID.randomUUID(),
              room.id(),
              r.organizer(),
              r.title(),
              r.attendeeCount(),
              r.start(),
              r.end());
      repo.save(booking);
      return BookingResponse.from(booking, room);
    } finally {
      lock.unlock();
    }
  }

  public Collection<BookingResponse> bookings(String roomId) {
    return repo.bookings().stream()
        .filter(b -> roomId == null || b.roomId().equals(roomId))
        .sorted(Comparator.comparing(Booking::start))
        .map(b -> BookingResponse.from(b, repo.room(b.roomId())))
        .toList();
  }

  public BookingResponse cancel(UUID id) {
    Booking b = repo.booking(id);
    b.cancel();
    return BookingResponse.from(b, repo.room(b.roomId()));
  }

  public Collection<RoomResponse> suggest(SuggestRoomRequest r) {
    validate(r.start(), r.end());
    return repo.rooms().stream()
        .filter(room -> room.capacity() >= r.attendeeCount())
        .filter(room -> room.features().containsAll(safe(r.features())))
        .filter(room -> available(room.id(), r.start(), r.end()))
        .sorted(Comparator.comparing(MeetingRoom::capacity))
        .map(RoomResponse::from)
        .toList();
  }

  private MeetingRoom firstAvailable(int count, Set<String> features, Instant start, Instant end) {
    return suggest(new SuggestRoomRequest(count, features, start, end)).stream()
        .map(rr -> repo.room(rr.id()))
        .findFirst()
        .orElseThrow(() -> new ConflictException("no room available"));
  }

  private boolean available(String roomId, Instant start, Instant end) {
    return repo.bookings().stream()
        .filter(b -> b.status() == BookingStatus.CONFIRMED)
        .filter(b -> b.roomId().equals(roomId))
        .noneMatch(b -> start.isBefore(b.end()) && end.isAfter(b.start()));
  }

  private static void validate(Instant start, Instant end) {
    if (!end.isAfter(start)) throw new BadRequestException("end must be after start");
  }

  private static Set<String> safe(Set<String> values) {
    return values == null ? Set.of() : values;
  }
}
