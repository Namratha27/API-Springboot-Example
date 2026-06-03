# Task Scheduler

## Interview Prompt
Design and implement a task scheduler API. Discuss priority queues, delayed execution, retries, cancellation, persistence, and worker ownership.

## Run

```bash
mvn -pl task-scheduler-api spring-boot:run
```

Base URL: `http://localhost:8085`

## Curl Commands

### Schedule a task

```bash
curl -X POST http://localhost:8085/tasks \
  -H 'Content-Type: application/json' \
  -d '{"name":"send-report","runAt":"2030-01-01T00:00:00Z","priority":5,"payload":{"reportId":"r1"}}'
```

### List tasks

```bash
curl http://localhost:8085/tasks
```

### Cancel task

```bash
curl -X DELETE http://localhost:8085/tasks/<task-id>
```

## What To Explain

- Priority queue orders by due time and then priority.
- Store tasks durably and use worker leases in production.
- Callback delivery should use timeouts, retry policy, and dead-letter handling.
