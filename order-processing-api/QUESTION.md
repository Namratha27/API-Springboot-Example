# Order Processing System

## Interview Prompt
Design and implement an order processing API. Discuss state machines, inventory reservation, payment authorization, idempotency, cancellation, and compensation.

## Run

```bash
mvn -pl order-processing-api spring-boot:run
```

Base URL: `http://localhost:8089`

## Curl Commands

### Create order

```bash
curl -X POST http://localhost:8089/orders -H 'Content-Type: application/json' -H 'Idempotency-Key: order-1' -d '{"customerId":"cust-1","lines":[{"sku":"IPHONE","quantity":1}],"totalAmount":999.00}'
```

### Get order

```bash
curl http://localhost:8089/orders/<order-id>
```

### Cancel order

```bash
curl -X POST http://localhost:8089/orders/<order-id>/cancel
```

## What To Explain

- Reserve inventory before payment authorization.
- Use idempotency keys for safe client retries.
- Compensate by releasing inventory and voiding payment on cancellation or failure.

## Staff-Level Answer Outline

- API: order creation is idempotent and returns a state-machine resource rather than hiding downstream work.
- Consistency: inventory reservation happens before payment authorization so failed payment can release reserved stock.
- Failure mode: downstream failures mark the order failed and run compensating release logic where needed.
- Production path: use a saga/outbox, persist state transitions, add payment idempotency, circuit breakers, and metrics for state counts and compensation failures.
