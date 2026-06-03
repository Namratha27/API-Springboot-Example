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

## Staff-Level Answer Outline

- API: tickets have explicit lifecycle commands for create, assign, comment, and transition instead of a vague update endpoint.
- Consistency: ticket state transitions guard invalid moves such as reopening a closed ticket.
- Failure mode: skill mismatch and closed-ticket reopen attempts are conflict responses.
- Production path: add audit events, search indexing, SLA policy tables, assignment queues, notifications, and metrics for backlog, breach rate, and agent load.
