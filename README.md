# API Spring Boot Example

Practical Spring Boot implementations for senior backend CoderPad preparation. Each prompt is an independent Maven module with its own REST API, domain service, validation, and interview notes.

This repo favors code you can explain under interview pressure: clear domain models, thread-safe in-memory stores, idempotency where it matters, outbound `RestClient` integrations for external dependencies, and extension points for production storage, queues, Redis, and observability.

## Modules

| Prompt | Module | Port | Main Concepts |
| --- | --- | ---: | --- |
| Design a File Upload Service | [`file-upload-service`](file-upload-service) | 8081 | multipart API, async processing, scanner/storage gateway |
| Design URL Shortener API | [`url-shortener-api`](url-shortener-api) | 8082 | REST, redirects, cache-friendly lookups, collision handling |
| Notification Service | [`notification-service`](notification-service) | 8083 | queues, retries, channel strategy, provider API calls |
| Rate Limiter | [`rate-limiter-api`](rate-limiter-api) | 8084 | filter, token bucket, thread safety, Redis-ready design |
| Task Scheduler | [`task-scheduler-api`](task-scheduler-api) | 8085 | priority queue, worker loop, callbacks, cancellation |
| Parking Lot | [`parking-lot-api`](parking-lot-api) | 8086 | LLD, allocation strategy, tickets, pricing |
| Meeting Room Scheduler | [`meeting-room-scheduler-api`](meeting-room-scheduler-api) | 8087 | intervals, conflict detection, room suggestions |
| Inventory Management API | [`inventory-management-api`](inventory-management-api) | 8088 | CRUD, stock reservations, idempotency, expiry |
| Order Processing System | [`order-processing-api`](order-processing-api) | 8089 | state machine, inventory/payment clients, compensation |
| Customer Support Ticket System | [`customer-support-ticket-api`](customer-support-ticket-api) | 8090 | ticket lifecycle, assignment, SLA escalation |

## Run

Build everything:

```bash
mvn clean test
```

Run one service:

```bash
mvn -pl url-shortener-api spring-boot:run
```

Try a request:

```bash
curl -X POST http://localhost:8082/links \
  -H 'Content-Type: application/json' \
  -d '{"originalUrl":"https://developer.apple.com/documentation/"}'
```

## Staff-Level Talking Points

- Start with API contracts, idempotency, and failure modes before code.
- State the production swap clearly: in-memory map to PostgreSQL/DynamoDB, local queue to Kafka/SQS, local token bucket to Redis/Lua.
- Mention backpressure, rate limits, timeouts, retries, and observability on every outbound dependency.
- Keep concurrency boring: per-aggregate locks, atomic operations, immutable response DTOs.
- Explain how each endpoint would scale from a single process to multiple instances.

## Useful Prep Links

- [Spring Boot reference documentation](https://docs.spring.io/spring-boot/index.html)
- [Spring Framework RestClient](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)
- [Spring validation](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html)
- [Apple jobs](https://jobs.apple.com/)
