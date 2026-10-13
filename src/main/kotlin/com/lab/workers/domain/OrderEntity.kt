package com.lab.workers.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "orders")
class OrderEntity(
    @Id
    val id: UUID,
    @Column(nullable = false)
    val customerEmail: String,
    @Column(nullable = false)
    val productSku: String,
    @Column(nullable = false)
    val quantity: Int,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus,
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column(nullable = false)
    var updatedAt: Instant = Instant.now(),
)
