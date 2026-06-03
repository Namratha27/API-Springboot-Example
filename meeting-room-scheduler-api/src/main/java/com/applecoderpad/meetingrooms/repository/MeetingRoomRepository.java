package com.applecoderpad.meetingrooms.repository;

import com.applecoderpad.meetingrooms.exception.NotFoundException;
import com.applecoderpad.meetingrooms.model.Booking;
import com.applecoderpad.meetingrooms.model.MeetingRoom;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class MeetingRoomRepository {
  private final Map<String, MeetingRoom> rooms = new ConcurrentHashMap<>();
  private final Map<UUID, Booking> bookings = new ConcurrentHashMap<>();

  public MeetingRoomRepository() {
    rooms.put(
        "apple-park-1",
        new MeetingRoom("apple-park-1", "Apple Park 1", 6, Set.of("video", "whiteboard")));
    rooms.put(
        "cupertino-12",
        new MeetingRoom(
            "cupertino-12", "Cupertino 12", 12, Set.of("video", "phone", "whiteboard")));
    rooms.put(
        "infinite-loop",
        new MeetingRoom("infinite-loop", "Infinite Loop", 20, Set.of("video", "projector")));
  }

  public Collection<MeetingRoom> rooms() {
    return rooms.values();
  }

  public MeetingRoom room(String id) {
    MeetingRoom room = rooms.get(id);
    if (room == null) throw new NotFoundException("room not found: " + id);
    return room;
  }

  public void save(Booking booking) {
    bookings.put(booking.id(), booking);
  }

  public Booking booking(UUID id) {
    Booking b = bookings.get(id);
    if (b == null) throw new NotFoundException("booking not found: " + id);
    return b;
  }

  public Collection<Booking> bookings() {
    return bookings.values();
  }
}
