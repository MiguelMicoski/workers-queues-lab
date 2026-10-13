package com.lab.workers.api.dto

import com.lab.workers.domain.OrderStatus
import java.time.Instant
import java.util.UUID

data class OrderResponse(
    val id: UUID,
    val status: OrderStatus,
    val message: String,
    val createdAt: Instant,
)
