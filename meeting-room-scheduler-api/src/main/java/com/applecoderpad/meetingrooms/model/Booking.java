package com.applecoderpad.meetingrooms.model;

import java.time.Instant;
import java.util.UUID;

public class Booking {
  private final UUID id;
  private final String roomId;
  private final String organizer;
  private final String title;
  private final int attendeeCount;
  private final Instant start;
  private final Instant end;
  private volatile BookingStatus status;

  private Booking(
      UUID id,
      String roomId,
      String organizer,
      String title,
      int attendeeCount,
      Instant start,
      Instant end) {
    this.id = id;
    this.roomId = roomId;
    this.organizer = organizer;
    this.title = title;
    this.attendeeCount = attendeeCount;
    this.start = start;
    this.end = end;
    this.status = BookingStatus.CONFIRMED;
  }

  public static Booking create(
      UUID id,
      String roomId,
      String organizer,
      String title,
      int attendeeCount,
      Instant start,
      Instant end) {
    return new Booking(id, roomId, organizer, title, attendeeCount, start, end);
  }

  public synchronized void cancel() {
    status = BookingStatus.CANCELED;
  }

  public UUID id() {
    return id;
  }

  public String roomId() {
    return roomId;
  }

  public String organizer() {
    return organizer;
  }

  public String title() {
    return title;
  }

  public int attendeeCount() {
    return attendeeCount;
  }

  public Instant start() {
    return start;
  }

  public Instant end() {
    return end;
  }

  public BookingStatus status() {
    return status;
  }
}
