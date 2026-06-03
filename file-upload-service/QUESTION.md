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
