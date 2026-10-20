package com.micoski.ordersync.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "dlq_records")
class DlqRecordEntity(
    @Id
    val id: UUID,
    @Column
    val orderId: UUID?,
    @Column
    val sqsMessageId: String?,
    @Column(nullable = false)
    val failureReason: String,
    @Column(nullable = false)
    val rawBody: String,
    @Column(nullable = false)
    val recordedAt: Instant = Instant.now(),
)
