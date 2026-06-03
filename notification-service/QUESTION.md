# Notification Service

## Interview Prompt
Design and implement a notification service. Discuss channel strategies, queues, retries, provider APIs, templates, idempotency, and delivery status tracking.

## Run

```bash
mvn -pl notification-service spring-boot:run
```

Base URL: `http://localhost:8083`

## Curl Commands

### Send notification

```bash
curl -X POST http://localhost:8083/notifications \
  -H 'Content-Type: application/json' \
  -d '{"recipient":"dev@example.com","subject":"Build finished","body":"Your job completed","channels":["EMAIL","PUSH"]}'
```

### List notifications

```bash
curl http://localhost:8083/notifications
```

### Get notification

```bash
curl http://localhost:8083/notifications/<notification-id>
```

## What To Explain

- Queue and retry behavior with exponential backoff.
- Provider clients use RestClient and can be wrapped with timeouts/circuit breakers.
- Replace the in-memory queue with Kafka, SQS, or RabbitMQ for production.
- Track delivery state per channel, not only per notification.
