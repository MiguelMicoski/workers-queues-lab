package com.micoski.ordersync.worker

import com.fasterxml.jackson.databind.ObjectMapper
import com.micoski.ordersync.external.ExternalOrderClient
import com.micoski.ordersync.messaging.OrderSubmittedEvent
import com.micoski.ordersync.service.IdempotencyService
import com.micoski.ordersync.service.OrderService
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

@Component
class OrderProcessingWorker(
    private val objectMapper: ObjectMapper,
    private val idempotencyService: IdempotencyService,
    private val orderService: OrderService,
    private val externalOrderClient: ExternalOrderClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @SqsListener("\${app.sqs.order-processing-queue}")
    fun handle(
        payload: String,
        @Header(name = "id", required = false) sqsMessageId: String?,
    ) {
        val event = objectMapper.readValue(payload, OrderSubmittedEvent::class.java)
        log.info("Processing order {} messageId={}", event.orderId, sqsMessageId)

        if (idempotencyService.alreadyProcessed(event.idempotencyKey)) {
            log.info("Skipping duplicate idempotencyKey={}", event.idempotencyKey)
            return
        }

        orderService.markProcessing(event.orderId)

        try {
            externalOrderClient.submitOrder(event)
            idempotencyService.recordSuccess(event.idempotencyKey, event.orderId)
            orderService.markSyncedExternal(event.orderId)
            log.info("Order {} submitted to partner", event.orderId)
        } catch (ex: PermanentProcessingException) {
            orderService.markFailed(event.orderId)
            log.error("Permanent error for order {}: {}", event.orderId, ex.message)
        } catch (ex: TransientProcessingException) {
            log.warn("Transient error for order {}, will retry: {}", event.orderId, ex.message)
            throw ex
        }
    }
}
