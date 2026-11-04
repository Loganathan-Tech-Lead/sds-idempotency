# 27. Idempotency-Key

**Category:** Resilience and consistency  |  **Status:** Working example included

Store key+response, replay on duplicates, 422 on body mismatch.

## Sequence
```mermaid
sequenceDiagram
    participant Client
    participant Payment
    participant Store
    Client->>Payment: POST /payments Idempotency-Key k-9
    Payment->>Store: SET NX
    Client->>Payment: retry same key
    Payment->>Client: 201 replayed
```

## Run
```bash
docker compose up -d
mvn spring-boot:run
```

## Part of
[system-design-concepts-master](../../README.md) - see the master repo for the full concept map.
