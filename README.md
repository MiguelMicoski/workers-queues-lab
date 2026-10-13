# workers-queues-lab

Laboratório em **Kotlin + Spring Boot** com **SNS**, **SQS** (via LocalStack) e **PostgreSQL** para fixar:

- processamento assíncrono
- mensageria desacoplada
- idempotência
- retries + DLQ
- transactional outbox

## Pré-requisitos

- JDK 17+
- Docker Desktop

## Subir infraestrutura

```bash
docker compose up -d
```

Aguarde Postgres e LocalStack saudáveis. O script `infra/localstack/init-aws.sh` cria:

- tópico SNS `order-events`
- fila `order-processing` (com redrive para DLQ após 3 recebimentos)
- fila DLQ `order-processing-dlq`
- subscription SNS → SQS (raw delivery)

## Rodar a aplicação

```bash
./gradlew bootRun
```

Windows: `gradlew.bat bootRun`

## Experimentos

### Caminho feliz

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d "{\"customerEmail\":\"you@example.com\",\"productSku\":\"SKU-1\",\"quantity\":2}"
```

Resposta **202 Accepted** com status `RECEIVED`. Depois de alguns segundos:

```bash
curl http://localhost:8080/api/lab/orders
```

Status esperado: `SYNCED_EXTERNAL`.

### Retries (falha transitória)

```json
"simulateExternalFailure": "transient"
```

O mock externo responde 503. O worker relança exceção → a mensagem volta à fila (visibility timeout). Após 3 tentativas, a mensagem vai para a **DLQ**; o `DlqWorker` registra em `dlq_records`.

### Falha permanente (sem enfileirar retries úteis)

```json
"simulateExternalFailure": "permanent"
```

O worker marca o pedido como `FAILED` e **confirma** a mensagem (não faz sentido repetir 422).

### Idempotência

Republique manualmente a mesma mensagem na fila ou crie outbox duplicada: o worker ignora se `idempotency_key` já existir. O mock externo também deduplica via header `Idempotency-Key`.

## Mapa do código

| Pacote | Papel |
|--------|--------|
| `api/OrderController` | API síncrona — aceita pedido |
| `service/OrderService` | Transação: pedido + outbox |
| `outbox/OutboxPublisher` | Job: outbox → SNS |
| `worker/OrderProcessingWorker` | Consome SQS, chama externo |
| `worker/DlqWorker` | Trata DLQ |
| `external/ExternalOrderClient` | Cliente HTTP com idempotency |
| `api/MockExternalOrderController` | Parceiro simulado |

## Fluxo (visão geral)

```
POST /api/orders
  → DB: orders + outbox_events (mesma transação)
  → 202 RECEIVED

OutboxPublisher (scheduled)
  → SNS order-events
  → SQS order-processing

OrderProcessingWorker
  → idempotência local
  → HTTP mock externo (Idempotency-Key)
  → sucesso: SYNCED_EXTERNAL
  → transitório: retry SQS
  → esgotado: DLQ → DlqWorker
```
