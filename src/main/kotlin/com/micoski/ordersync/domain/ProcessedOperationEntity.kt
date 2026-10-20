package com.micoski.ordersync.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "processed_operations")
class ProcessedOperationEntity(
    @Id
    @Column(name = "idempotency_key")
    val idempotencyKey: String,
    @Column(nullable = false)
    val orderId: UUID,
    @Column(nullable = false)
    val outcome: String,
    @Column(nullable = false)
    val processedAt: Instant = Instant.now(),
)
