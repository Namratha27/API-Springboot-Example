package com.applecoderpad.meetingrooms;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootApplication
public class MeetingRoomSchedulerApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(MeetingRoomSchedulerApiApplication.class, args);
    }
}

@RestController
@RequestMapping("/meeting-rooms")
class MeetingRoomController {
    private final MeetingRoomService meetingRooms;

    MeetingRoomController(MeetingRoomService meetingRooms) {
        this.meetingRooms = meetingRooms;
    }

    @GetMapping
    Collection<RoomResponse> rooms() {
        return meetingRooms.rooms();
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    BookingResponse book(@Valid @RequestBody CreateBookingRequest request) {
        return meetingRooms.book(request);
    }

    @GetMapping("/bookings")
    Collection<BookingResponse> bookings(@RequestParam(required = false) String roomId) {
        return meetingRooms.bookings(roomId);
    }

    @DeleteMapping("/bookings/{bookingId}")
    BookingResponse cancel(@PathVariable UUID bookingId) {
        return meetingRooms.cancel(bookingId);
    }

    @PostMapping("/suggestions")
    Collection<RoomResponse> suggest(@Valid @RequestBody SuggestRoomRequest request) {
        return meetingRooms.suggest(request);
    }
}

@Service
class MeetingRoomService {
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, MeetingRoom> rooms = new ConcurrentHashMap<>();
    private final Map<UUID, Booking> bookings = new ConcurrentHashMap<>();

    MeetingRoomService() {
        seedRooms();
    }

    Collection<RoomResponse> rooms() {
        return rooms.values().stream().map(RoomResponse::from).toList();
    }

    BookingResponse book(CreateBookingRequest request) {
        validateRange(request.start(), request.end());
        lock.lock();
        try {
            MeetingRoom room = request.roomId() == null || request.roomId().isBlank()
                    ? firstAvailableRoom(request.attendeeCount(), request.features(), request.start(), request.end())
                    : getRoom(request.roomId());
            ensureAvailable(room.id(), request.start(), request.end(), null);
            if (room.capacity() < request.attendeeCount()) {
                throw new ConflictException("room capacity is too small");
            }
            if (!room.features().containsAll(nullSafe(request.features()))) {
                throw new ConflictException("room does not satisfy requested features");
            }
            Booking booking = Booking.create(
                    UUID.randomUUID(),
                    room.id(),
                    request.organizer(),
                    request.title(),
                    request.attendeeCount(),
                    request.start(),
                    request.end()
            );
            bookings.put(booking.id(), booking);
            return BookingResponse.from(booking, room);
        } finally {
            lock.unlock();
        }
    }

    Collection<BookingResponse> bookings(String roomId) {
        return bookings.values().stream()
                .filter(booking -> roomId == null || booking.roomId().equals(roomId))
                .sorted(Comparator.comparing(Booking::start))
                .map(booking -> BookingResponse.from(booking, getRoom(booking.roomId())))
                .toList();
    }

    BookingResponse cancel(UUID bookingId) {
        lock.lock();
        try {
            Booking booking = getBooking(bookingId);
            booking.cancel();
            return BookingResponse.from(booking, getRoom(booking.roomId()));
        } finally {
            lock.unlock();
        }
    }

    Collection<RoomResponse> suggest(SuggestRoomRequest request) {
        validateRange(request.start(), request.end());
        lock.lock();
        try {
            return rooms.values().stream()
                    .filter(room -> room.capacity() >= request.attendeeCount())
                    .filter(room -> room.features().containsAll(nullSafe(request.features())))
                    .filter(room -> available(room.id(), request.start(), request.end(), null))
                    .sorted(Comparator.comparing(MeetingRoom::capacity))
                    .map(RoomResponse::from)
                    .toList();
        } finally {
            lock.unlock();
        }
    }

    private MeetingRoom firstAvailableRoom(int attendeeCount, Set<String> features, Instant start, Instant end) {
        return suggest(new SuggestRoomRequest(attendeeCount, features, start, end)).stream()
                .map(response -> rooms.get(response.id()))
                .findFirst()
                .orElseThrow(() -> new ConflictException("no room available"));
    }

    private void ensureAvailable(String roomId, Instant start, Instant end, UUID ignoredBookingId) {
        if (!available(roomId, start, end, ignoredBookingId)) {
            throw new ConflictException("room already booked for requested interval");
        }
    }

    private boolean available(String roomId, Instant start, Instant end, UUID ignoredBookingId) {
        return bookings.values().stream()
                .filter(booking -> booking.status() == BookingStatus.CONFIRMED)
                .filter(booking -> booking.roomId().equals(roomId))
                .filter(booking -> ignoredBookingId == null || !booking.id().equals(ignoredBookingId))
                .noneMatch(booking -> overlaps(start, end, booking.start(), booking.end()));
    }

    private static boolean overlaps(Instant leftStart, Instant leftEnd, Instant rightStart, Instant rightEnd) {
        return leftStart.isBefore(rightEnd) && leftEnd.isAfter(rightStart);
    }

    private static void validateRange(Instant start, Instant end) {
        if (!end.isAfter(start)) {
            throw new BadRequestException("end must be after start");
        }
    }

    private MeetingRoom getRoom(String roomId) {
        MeetingRoom room = rooms.get(roomId);
        if (room == null) {
            throw new NotFoundException("room not found: " + roomId);
        }
        return room;
    }

    private Booking getBooking(UUID bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new NotFoundException("booking not found: " + bookingId);
        }
        return booking;
    }

    private static Set<String> nullSafe(Set<String> values) {
        return values == null ? Set.of() : values;
    }

    private void seedRooms() {
        rooms.put("apple-park-1", new MeetingRoom("apple-park-1", "Apple Park 1", 6, Set.of("video", "whiteboard")));
        rooms.put("cupertino-12", new MeetingRoom("cupertino-12", "Cupertino 12", 12, Set.of("video", "phone", "whiteboard")));
        rooms.put("infinite-loop", new MeetingRoom("infinite-loop", "Infinite Loop", 20, Set.of("video", "projector")));
    }
}

record MeetingRoom(String id, String name, int capacity, Set<String> features) {
}

class Booking {
    private final UUID id;
    private final String roomId;
    private final String organizer;
    private final String title;
    private final int attendeeCount;
    private final Instant start;
    private final Instant end;
    private volatile BookingStatus status;

    private Booking(UUID id, String roomId, String organizer, String title, int attendeeCount, Instant start, Instant end) {
        this.id = id;
        this.roomId = roomId;
        this.organizer = organizer;
        this.title = title;
        this.attendeeCount = attendeeCount;
        this.start = start;
        this.end = end;
        this.status = BookingStatus.CONFIRMED;
    }

    static Booking create(UUID id, String roomId, String organizer, String title, int attendeeCount, Instant start, Instant end) {
        return new Booking(id, roomId, organizer, title, attendeeCount, start, end);
    }

    synchronized void cancel() {
        status = BookingStatus.CANCELED;
    }

    UUID id() {
        return id;
    }

    String roomId() {
        return roomId;
    }

    String organizer() {
        return organizer;
    }

    String title() {
        return title;
    }

    int attendeeCount() {
        return attendeeCount;
    }

    Instant start() {
        return start;
    }

    Instant end() {
        return end;
    }

    BookingStatus status() {
        return status;
    }
}

record CreateBookingRequest(String roomId,
                            @NotBlank String organizer,
                            @NotBlank String title,
                            @Min(1) int attendeeCount,
                            Set<String> features,
                            @Future Instant start,
                            @Future Instant end) {
}

record SuggestRoomRequest(@Min(1) int attendeeCount,
                          Set<String> features,
                          @Future Instant start,
                          @Future Instant end) {
}

record RoomResponse(String id, String name, int capacity, Set<String> features) {
    static RoomResponse from(MeetingRoom room) {
        return new RoomResponse(room.id(), room.name(), room.capacity(), room.features());
    }
}

record BookingResponse(UUID id,
                       String roomId,
                       String roomName,
                       String organizer,
                       String title,
                       int attendeeCount,
                       Instant start,
                       Instant end,
                       BookingStatus status) {
    static BookingResponse from(Booking booking, MeetingRoom room) {
        return new BookingResponse(
                booking.id(),
                booking.roomId(),
                room.name(),
                booking.organizer(),
                booking.title(),
                booking.attendeeCount(),
                booking.start(),
                booking.end(),
                booking.status()
        );
    }
}

enum BookingStatus {
    CONFIRMED, CANCELED
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException extends RuntimeException {
    NotFoundException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.CONFLICT)
class ConflictException extends RuntimeException {
    ConflictException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequestException extends RuntimeException {
    BadRequestException(String message) {
        super(message);
    }
}
