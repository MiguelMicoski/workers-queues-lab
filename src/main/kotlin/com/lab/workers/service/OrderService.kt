package com.lab.workers.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.lab.workers.api.dto.CreateOrderRequest
import com.lab.workers.api.dto.OrderResponse
import com.lab.workers.domain.OrderEntity
import com.lab.workers.domain.OrderStatus
import com.lab.workers.domain.OutboxEventEntity
import com.lab.workers.messaging.OrderSubmittedEvent
import com.lab.workers.persistence.OrderRepository
import com.lab.workers.persistence.OutboxEventRepository
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
    /**
     * Contrato síncrono: valida, persiste pedido + outbox na mesma transação.
     * Integração externa fica para o worker (assíncrono).
     */
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
                simulateExternalFailure = request.simulateExternalFailure?.trim()?.lowercase(),
            )
        val outbox =
            OutboxEventEntity(
                id = UUID.randomUUID(),
                aggregateType = "Order",
                aggregateId = orderId,
                eventType = event.eventType,
                payload = objectMapper.writeValueAsString(event),
            )
        outboxEventRepository.save(outbox)

        return OrderResponse(
            id = orderId,
            status = OrderStatus.RECEIVED,
            message =
                "Pedido aceito. Envio ao parceiro ocorrerá em segundo plano " +
                    "(outbox → SNS → SQS → worker).",
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
