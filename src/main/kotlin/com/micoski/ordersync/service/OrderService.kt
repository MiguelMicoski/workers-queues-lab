package com.micoski.ordersync.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.micoski.ordersync.api.dto.CreateOrderRequest
import com.micoski.ordersync.api.dto.OrderResponse
import com.micoski.ordersync.domain.OrderEntity
import com.micoski.ordersync.domain.OrderStatus
import com.micoski.ordersync.domain.OutboxEventEntity
import com.micoski.ordersync.messaging.OrderSubmittedEvent
import com.micoski.ordersync.persistence.OrderRepository
import com.micoski.ordersync.persistence.OutboxEventRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val outboxEventRepository: OutboxEventRepository,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun createOrder(request: CreateOrderRequest): OrderResponse {
        val orderId = UUID.randomUUID()
        val order =
            OrderEntity(
                id = orderId,
                customerEmail = request.customerEmail.trim(),
                productSku = request.productSku.trim(),
                quantity = request.quantity,
                status = OrderStatus.RECEIVED,
            )
        orderRepository.save(order)

        val event =
            OrderSubmittedEvent(
                orderId = orderId,
                customerEmail = order.customerEmail,
                productSku = order.productSku,
                quantity = order.quantity,
                idempotencyKey = IdempotencyKeys.externalSubmit(orderId),
            )
        outboxEventRepository.save(
            OutboxEventEntity(
                id = UUID.randomUUID(),
                aggregateType = "Order",
                aggregateId = orderId,
                eventType = event.eventType,
                payload = objectMapper.writeValueAsString(event),
            ),
        )

        return OrderResponse(
            id = orderId,
            status = OrderStatus.RECEIVED,
            createdAt = order.createdAt,
        )
    }

    fun getOrder(orderId: UUID): OrderResponse {
        val order =
            orderRepository.findById(orderId).orElseThrow {
                NoSuchElementException("Order not found: $orderId")
            }
        return OrderResponse(
            id = order.id,
            status = order.status,
            createdAt = order.createdAt,
        )
    }

    @Transactional
    fun markProcessing(orderId: UUID) {
        updateStatus(orderId, OrderStatus.PROCESSING)
    }

    @Transactional
    fun markSyncedExternal(orderId: UUID) {
        updateStatus(orderId, OrderStatus.SYNCED_EXTERNAL)
    }

    @Transactional
    fun markFailed(orderId: UUID) {
        updateStatus(orderId, OrderStatus.FAILED)
    }

    private fun updateStatus(orderId: UUID, status: OrderStatus) {
        val order =
            orderRepository.findById(orderId).orElseThrow {
                IllegalArgumentException("Order not found: $orderId")
            }
        order.status = status
        order.updatedAt = Instant.now()
    }
}
