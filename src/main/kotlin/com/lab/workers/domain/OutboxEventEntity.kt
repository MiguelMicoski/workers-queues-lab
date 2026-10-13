package com.lab.workers.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "outbox_events")
class OutboxEventEntity(
    @Id
    val id: UUID,
    @Column(nullable = false)
    val aggregateType: String,
    @Column(nullable = false)
    val aggregateId: UUID,
    @Column(nullable = false)
    val eventType: String,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    val payload: String,
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column
    var publishedAt: Instant? = null,
)
