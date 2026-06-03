package com.applecoderpad.meetingrooms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "room_bookings")
public class Booking {
  @Id private UUID id;

  @Column(nullable = false)
  private String roomId;

  @Column(nullable = false)
  private String organizer;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private int attendeeCount;

  @Column(name = "start_at", nullable = false)
  private Instant start;

  @Column(name = "end_at", nullable = false)
  private Instant end;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private volatile BookingStatus status;

  protected Booking() {}

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
