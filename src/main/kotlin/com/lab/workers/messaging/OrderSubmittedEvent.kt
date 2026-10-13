package com.lab.workers.messaging

import java.util.UUID

/**
 * Payload publicado via Outbox → SNS → SQS.
 * [idempotencyKey] é estável: mesmo pedido sempre gera a mesma chave de operação.
 */
data class OrderSubmittedEvent(
    val orderId: UUID,
    val customerEmail: String,
    val productSku: String,
    val quantity: Int,
    val idempotencyKey: String,
    val eventType: String = "OrderSubmitted",
    val simulateExternalFailure: String? = null,
)
