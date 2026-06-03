# Meeting Room Scheduler

## Interview Prompt
Design and implement a meeting room scheduler API. Discuss interval conflicts, room suggestions, recurring meetings, calendars, and concurrency.

## Run

```bash
mvn -pl meeting-room-scheduler-api spring-boot:run
```

Base URL: `http://localhost:8087`

## Curl Commands

### List rooms

```bash
curl http://localhost:8087/meeting-rooms
```

### Suggest rooms

```bash
curl -X POST http://localhost:8087/meeting-rooms/suggestions -H 'Content-Type: application/json' -d '{"attendeeCount":4,"features":["video"],"start":"2030-01-01T16:00:00Z","end":"2030-01-01T17:00:00Z"}'
```

### Book a room

```bash
curl -X POST http://localhost:8087/meeting-rooms/bookings -H 'Content-Type: application/json' -d '{"organizer":"alice","title":"Design review","attendeeCount":4,"features":["video"],"start":"2030-01-01T16:00:00Z","end":"2030-01-01T17:00:00Z"}'
```

## What To Explain

- Interval overlap checks prevent double booking.
- Room suggestions sort by smallest sufficient capacity.
- Production designs need calendar sync, recurring rules, and optimistic locking.

## Staff-Level Answer Outline

- API: suggest and book are separate so clients can preview options without committing a booking.
- Consistency: overlap detection runs under a lock in-memory; production should enforce it with transactions or optimistic locking.
- Failure mode: invalid intervals and overlapping bookings return precise bad-request/conflict responses.
- Production path: add recurring rules, external calendar sync, room blackouts, pagination, and indexes on room/start/end.
