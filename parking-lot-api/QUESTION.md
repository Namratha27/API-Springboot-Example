# Parking Lot

## Interview Prompt
Design and implement a parking lot API. Discuss spot allocation, ticket lifecycle, pricing, concurrency, and extensibility for vehicle or spot types.

## Run

```bash
mvn -pl parking-lot-api spring-boot:run
```

Base URL: `http://localhost:8086`

## Curl Commands

### Park a car

```bash
curl -X POST http://localhost:8086/parking/tickets \
  -H 'Content-Type: application/json' \
  -d '{"licensePlate":"ABC123","vehicleType":"CAR","ev":false}'
```

### Check availability

```bash
curl http://localhost:8086/parking/availability
```

### Exit parking

```bash
curl -X POST http://localhost:8086/parking/tickets/<ticket-id>/exit
```

## What To Explain

- Spot allocation is locked to avoid double assignment.
- Add pricing strategies instead of hard-coded rates for production.
- Persist open tickets and spot occupancy in a database.

## Staff-Level Answer Outline

- API: parking creates a ticket, exit closes the ticket, and availability is derived from spot state.
- Consistency: allocation and exit are locked so two cars cannot take the same spot or close the same ticket twice.
- Failure mode: no compatible spot and duplicate exit are conflict cases, not generic server failures.
- Production path: add pricing strategy objects, persisted occupancy, payment integration, event audit, and floor/type indexes for fast allocation.
