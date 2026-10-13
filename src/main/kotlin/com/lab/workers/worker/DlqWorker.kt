package com.lab.workers.worker

import com.fasterxml.jackson.databind.ObjectMapper
import com.lab.workers.domain.DlqRecordEntity
import com.lab.workers.messaging.OrderSubmittedEvent
import com.lab.workers.persistence.DlqRecordRepository
import com.lab.workers.service.OrderService
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Fluxo de tratamento quando retries esgotam (Dead Letter Queue).
 */
@Component
class DlqWorker(
    private val objectMapper: ObjectMapper,
    private val dlqRecordRepository: DlqRecordRepository,
    private val orderService: OrderService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @SqsListener("\${app.sqs.order-processing-dlq}")
    @Transactional
    fun handle(
        payload: String,
        @Header(name = "id", required = false) sqsMessageId: String?,
    ) {
        log.error("DLQ message received: sqsMessageId={}", sqsMessageId)

        val orderId =
            runCatching {
                objectMapper.readValue(payload, OrderSubmittedEvent::class.java).orderId
            }.getOrNull()

        if (orderId != null) {
            orderService.markFailed(orderId)
        }

        dlqRecordRepository.save(
            DlqRecordEntity(
                id = UUID.randomUUID(),
                orderId = orderId,
                sqsMessageId = sqsMessageId,
                failureReason = "Retries exhausted or redrive from main queue",
                rawBody = payload,
            ),
        )
    }
}
