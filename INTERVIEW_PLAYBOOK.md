# Staff-Level API Interview Playbook

Use this repo to show practical backend judgment, not just syntax. In a CoderPad round, talk through the contract first, code the smallest reliable version, then explain the production upgrade path.

## Opening Structure

1. Clarify traffic, data durability, consistency, and failure tolerance.
2. Write the API contract and domain states before implementation details.
3. Call out the single-process simplification and the production replacement.
4. Add one happy-path test and one failure/idempotency/concurrency test.
5. End with scale, observability, and operational risks.

## Cross-Cutting Signals

- API errors: return structured `ProblemDetail` responses for validation, conflicts, missing resources, expired links, and rate limits.
- Persistence: map durable domain objects with `@Entity`, `@Table`, `@Id`, and Spring Data `JpaRepository` interfaces.
- Idempotency: use client-provided keys for order and inventory retry safety.
- Concurrency: protect aggregate state with locks or synchronized methods where in-memory state can be mutated.
- Outbound calls: configure `RestClient` with timeouts and explain retries, circuit breakers, and dead-letter behavior.
- Storage swap: replace repositories with PostgreSQL/DynamoDB, queues with Kafka/SQS, and local buckets with Redis/Lua.
- Observability: add request IDs, metrics for latency/errors, queue depth, retry count, and business counters.
- Security: validate request bodies, bound upload sizes, avoid trusting headers blindly, and scope records by tenant/user.

## Module Talking Points

| Module | Staff-Level Angle |
| --- | --- |
| File Upload Service | Acknowledge upload streaming, async malware scan, object storage, checksum-based audit, and backpressure. |
| URL Shortener API | Keep redirects fast, isolate analytics writes, handle alias collisions, and cache hot links. |
| Notification Service | Model delivery per channel, retry with backoff, isolate provider failures, and track delivery state. |
| Rate Limiter | Use token bucket locally, then move counters to Redis with atomic Lua for multi-instance correctness. |
| Task Scheduler | Separate durable task records from worker leases, retry callbacks, and dead-letter exhausted tasks. |
| Parking Lot | Explain allocation strategy, ticket lifecycle, pricing extensibility, and locked spot assignment. |
| Meeting Room Scheduler | Prove interval overlap logic, discuss optimistic locking, recurring meetings, and calendar sync. |
| Inventory Management API | Prevent oversell with reservations, idempotency, row locks, and reservation expiry. |
| Order Processing System | Use a state machine, reserve inventory before payment, and compensate failed/canceled workflows. |
| Customer Support Ticket System | Explain assignment capacity, skills, SLA escalation, audit trail, and search indexing. |

## Scale From 1K+ RPS

- Keep API nodes stateless and horizontally scalable.
- Push slow work to queues and return status resources for polling.
- Protect downstream systems with timeouts, bulkheads, retries with jitter, and circuit breakers.
- Avoid hot keys by sharding counters, partitioning queues, and caching read-heavy paths.
- Add pagination to list endpoints before real traffic.
- Use database constraints for uniqueness and transactions for multi-record invariants.

## Red Flags To Avoid Saying

- "I would just use a HashMap in production."
- "Retries always fix transient failures."
- "The database will handle scaling automatically."
- "I do not need idempotency because clients should not retry."
- "I will add observability later."

## Strong Closing

End by saying what you intentionally simplified for CoderPad and exactly how you would replace it in production. That shows judgment: you can ship a clear first version and you know where the production risks live.
