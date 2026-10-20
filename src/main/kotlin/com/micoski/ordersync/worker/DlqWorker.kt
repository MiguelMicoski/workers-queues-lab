package com.micoski.ordersync.worker

import com.fasterxml.jackson.databind.ObjectMapper
import com.micoski.ordersync.domain.DlqRecordEntity
import com.micoski.ordersync.messaging.OrderSubmittedEvent
import com.micoski.ordersync.persistence.DlqRecordRepository
import com.micoski.ordersync.service.OrderService
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

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
        log.error("Processing DLQ message messageId={}", sqsMessageId)

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
                failureReason = "Max receive count exceeded on order-processing queue",
                rawBody = payload,
            ),
        )
    }
}
