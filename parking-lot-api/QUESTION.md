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
