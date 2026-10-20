package com.micoski.ordersync.api.dto

import com.micoski.ordersync.domain.OrderStatus
import java.time.Instant
import java.util.UUID

data class OrderResponse(
    val id: UUID,
    val status: OrderStatus,
    val createdAt: Instant,
)
