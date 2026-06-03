# File Upload Service

## Interview Prompt
Design and implement a file upload service as a production-minded Spring Boot API. Discuss multipart limits, async processing, malware scanning, object storage, metadata, and status polling.

## Run

```bash
mvn -pl file-upload-service spring-boot:run
```

Base URL: `http://localhost:8081`

## Curl Commands

### Upload a file

```bash
curl -X POST http://localhost:8081/uploads \
  -F 'owner=alice' \
  -F 'file=@README.md'
```

### List uploads

```bash
curl http://localhost:8081/uploads
```

### Get one upload

```bash
curl http://localhost:8081/uploads/<upload-id>
```

## What To Explain

- Async scan and storage processing keeps uploads responsive.
- Checksums support dedupe, integrity, and audit trails.
- Swap the in-memory repository for PostgreSQL plus object storage metadata.
- Add queue backpressure, size limits, metrics, trace IDs, and retry policies.

## Staff-Level Answer Outline

- API: `POST /uploads` accepts multipart input and returns a status resource instead of blocking on scan/storage.
- Consistency: metadata is written before async processing so clients can poll even if the worker is still running.
- Failure mode: empty and oversized uploads fail fast; scanner/storage failures move the upload to a terminal failed state.
- Production path: stream bytes to object storage, publish scan jobs to a queue, persist metadata, and emit metrics for queue depth, scan latency, and rejection count.
