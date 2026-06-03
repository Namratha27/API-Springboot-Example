# Customer Support Ticket System

## Interview Prompt
Design and implement a customer support ticket API. Discuss ticket lifecycle, assignment, comments, SLAs, escalation, and agent capacity.

## Run

```bash
mvn -pl customer-support-ticket-api spring-boot:run
```

Base URL: `http://localhost:8090`

## Curl Commands

### Create ticket

```bash
curl -X POST http://localhost:8090/tickets -H 'Content-Type: application/json' -d '{"customerId":"cust-1","category":"api","subject":"API issue","description":"Cannot call endpoint","priority":"HIGH"}'
```

### Add comment

```bash
curl -X POST http://localhost:8090/tickets/<ticket-id>/comments -H 'Content-Type: application/json' -d '{"author":"agent-platform","body":"Investigating","internal":false}'
```

### List agents

```bash
curl http://localhost:8090/agents
```

## What To Explain

- Auto-assignment checks skill match and active capacity.
- SLA escalation runs as a scheduled process.
- Production systems need audit history, notifications, and search indexing.
