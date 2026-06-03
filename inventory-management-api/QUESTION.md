# Inventory Management API

## Interview Prompt
Design and implement an inventory management API. Discuss CRUD, stock adjustments, reservation, commit/release, idempotency, and oversell prevention.

## Run

```bash
mvn -pl inventory-management-api spring-boot:run
```

Base URL: `http://localhost:8088`

## Curl Commands

### Create product

```bash
curl -X POST http://localhost:8088/products -H 'Content-Type: application/json' -d '{"sku":"IPHONE","name":"iPhone","onHand":10,"reorderThreshold":2}'
```

### Reserve stock

```bash
curl -X POST http://localhost:8088/reservations -H 'Content-Type: application/json' -H 'Idempotency-Key: order-1-reserve' -d '{"orderId":"order-1","lines":[{"sku":"IPHONE","quantity":2}]}'
```

### Commit reservation

```bash
curl -X POST http://localhost:8088/reservations/<reservation-id>/commit
```

## What To Explain

- Reserve before payment to avoid overselling.
- Idempotency keys make retries safe.
- Use database transactions and row-level locks in production.
