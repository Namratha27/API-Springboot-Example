# API Spring Boot Example

Practical Spring Boot implementations for senior backend CoderPad preparation. Each prompt is an independent Maven module with its own REST API, domain service, validation, and interview notes.

This repo favors code you can explain under interview pressure: clear domain models, thread-safe in-memory stores, idempotency where it matters, outbound `RestClient` integrations for external dependencies, and extension points for production storage, queues, Redis, and observability.

Start with the [`INTERVIEW_PLAYBOOK.md`](INTERVIEW_PLAYBOOK.md) when practicing the verbal walkthrough.

## Modules

| Prompt | Module | Port | Main Concepts |
| --- | --- | ---: | --- |
| Design a File Upload Service | [`file-upload-service`](file-upload-service/QUESTION.md) | 8081 | multipart API, async processing, scanner/storage gateway |
| Design URL Shortener API | [`url-shortener-api`](url-shortener-api/QUESTION.md) | 8082 | REST, redirects, cache-friendly lookups, collision handling |
| Notification Service | [`notification-service`](notification-service/QUESTION.md) | 8083 | queues, retries, channel strategy, provider API calls |
| Rate Limiter | [`rate-limiter-api`](rate-limiter-api/QUESTION.md) | 8084 | filter, token bucket, thread safety, Redis-ready design |
| Task Scheduler | [`task-scheduler-api`](task-scheduler-api/QUESTION.md) | 8085 | priority queue, worker loop, callbacks, cancellation |
| Parking Lot | [`parking-lot-api`](parking-lot-api/QUESTION.md) | 8086 | LLD, allocation strategy, tickets, pricing |
| Meeting Room Scheduler | [`meeting-room-scheduler-api`](meeting-room-scheduler-api/QUESTION.md) | 8087 | intervals, conflict detection, room suggestions |
| Inventory Management API | [`inventory-management-api`](inventory-management-api/QUESTION.md) | 8088 | CRUD, stock reservations, idempotency, expiry |
| Order Processing System | [`order-processing-api`](order-processing-api/QUESTION.md) | 8089 | state machine, inventory/payment clients, compensation |
| Customer Support Ticket System | [`customer-support-ticket-api`](customer-support-ticket-api/QUESTION.md) | 8090 | ticket lifecycle, assignment, SLA escalation |

## Folder Layout

Every module is split into interview-friendly packages:

```text
controller/   HTTP endpoints
dto/          request and response records
model/        domain objects and enums
repository/   in-memory persistence boundary
service/      business logic
client/       outbound REST clients, where needed
filter/       HTTP filters, where needed
exception/    API exceptions and response statuses
```

Each module also includes:

- `QUESTION.md` with the prompt, run command, curl commands, and talking points.
- Spring Boot tests that cover one core behavior plus one edge case, idempotency case, or failure invariant.

## Staff-Level Signals In The Code

- Structured `ProblemDetail` API errors via `ApiExceptionHandler` classes.
- Configured outbound `RestClient` beans with connect/read timeouts.
- Rate-limit 429 responses include retry headers and problem JSON.
- Domain state transitions guard invalid operations such as reopening closed tickets or exiting a closed parking ticket.
- In-memory repositories are explicit boundaries that can be replaced by production storage.

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
