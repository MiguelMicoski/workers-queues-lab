# order-sync

Kotlin service that accepts orders over HTTP and forwards them to a partner API asynchronously using **transactional outbox**, **Amazon SNS/SQS**, idempotent workers, retries, and a dead-letter queue.

Built as a reference implementation for reliable async integration patterns (LocalStack for local messaging, PostgreSQL for persistence).

## Stack

- Kotlin, Spring Boot 3
- PostgreSQL, Flyway
- AWS SNS + SQS (Spring Cloud AWS; LocalStack locally)

## Architecture

```
POST /api/orders
  → orders + outbox_events (single DB transaction)
  → 202 Accepted

Outbox relay (scheduled)
  → SNS topic order-events
  → SQS queue order-processing

OrderProcessingWorker
  → idempotency check
  → HTTP POST to partner (Idempotency-Key)
  → order status SYNCED_EXTERNAL | FAILED

After max receives on main queue
  → order-processing-dlq → DlqWorker
```

## Run locally

Requirements: JDK 17+, Docker.

```bash
docker compose up -d
./gradlew bootRun --args='--spring.profiles.active=local'
```

Windows:

```powershell
docker compose up -d
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

The `local` profile enables LocalStack endpoints and an in-process partner webhook at `/partner/orders` for end-to-end runs without an external system.

## API

**Create order**

```http
POST /api/orders
Content-Type: application/json

{
  "customerEmail": "customer@example.com",
  "productSku": "SKU-42",
  "quantity": 1
}
```

**Get order**

```http
GET /api/orders/{uuid}
```

## Configuration

| Variable | Default (local) | Description |
|----------|-----------------|-------------|
| `PARTNER_API_BASE_URL` | `http://localhost:8080/partner` | Partner HTTP API base URL |
| `AWS_ENDPOINT` | `http://localhost:4566` with profile `local` | SNS/SQS endpoint |
| `DB_*` | see `application.yml` | PostgreSQL |

In AWS, omit `AWS_ENDPOINT` and use real IAM credentials; point `PARTNER_API_BASE_URL` at your partner service.
