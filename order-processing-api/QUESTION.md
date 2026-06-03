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
