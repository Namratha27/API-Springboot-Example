# URL Shortener API

## Interview Prompt
Design and implement a URL shortener API. Discuss REST contracts, alias collisions, redirects, analytics, caching, and storage design.

## Run

```bash
mvn -pl url-shortener-api spring-boot:run
```

Base URL: `http://localhost:8082`

## Curl Commands

### Create short link

```bash
curl -X POST http://localhost:8082/links \
  -H 'Content-Type: application/json' \
  -d '{"originalUrl":"https://developer.apple.com","customAlias":"appledocs"}'
```

### Inspect short link

```bash
curl http://localhost:8082/links/appledocs
```

### Follow redirect

```bash
curl -i http://localhost:8082/appledocs
```

### Disable link

```bash
curl -X DELETE http://localhost:8082/links/appledocs
```

## What To Explain

- Code generation and collision handling.
- Cache hot short codes at the edge or in Redis.
- Store click analytics asynchronously to keep redirects fast.
- Use permanent or temporary redirects deliberately.

## Staff-Level Answer Outline

- API: separate link creation/inspection endpoints from the redirect endpoint so operational reads do not slow redirects.
- Consistency: custom aliases use uniqueness checks; production should enforce that with a database unique index.
- Failure mode: disabled or expired links return explicit gone/not-found semantics instead of silently redirecting.
- Production path: cache hot codes, shard by code prefix, write analytics asynchronously, and protect creation with rate limits.
