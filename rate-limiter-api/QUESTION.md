# Rate Limiter

## Interview Prompt
Design and implement a rate limiter API for backend services. Discuss token bucket behavior, Redis/Lua alternatives, distributed correctness, and HTTP filter integration.

## Run

```bash
mvn -pl rate-limiter-api spring-boot:run
```

Base URL: `http://localhost:8084`

## Curl Commands

### Configure a client policy

```bash
curl -X PUT http://localhost:8084/rate-limit/policies/demo-key \
  -H 'Content-Type: application/json' \
  -d '{"capacity":2,"refillPerSecond":1}'
```

### Hit protected endpoint

```bash
curl -i http://localhost:8084/api/demo -H 'X-API-Key: demo-key'
```

### List policies

```bash
curl http://localhost:8084/rate-limit/policies
```

## What To Explain

- Token bucket allows bursts up to capacity while smoothing sustained traffic.
- In production, store counters in Redis with Lua for atomic multi-instance behavior.
- Return rate limit headers so clients can back off cleanly.

## Staff-Level Answer Outline

- API: policy configuration is separate from protected endpoints, and the filter enforces limits before controller work runs.
- Consistency: local buckets are synchronized for one process; production needs Redis/Lua or another atomic shared counter.
- Failure mode: 429 responses include retry information and structured problem JSON.
- Production path: add tenant-aware keys, route-level policies, shadow-mode rollout, metrics for drops/allowed requests, and protection for hot clients.
