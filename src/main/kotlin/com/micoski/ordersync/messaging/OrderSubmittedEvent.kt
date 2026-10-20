package com.micoski.ordersync.messaging

import java.util.UUID

data class OrderSubmittedEvent(
    val orderId: UUID,
    val customerEmail: String,
    val productSku: String,
    val quantity: Int,
    val idempotencyKey: String,
    val eventType: String = "OrderSubmitted",
)
