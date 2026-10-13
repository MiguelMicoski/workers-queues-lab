package com.lab.workers.outbox

import com.lab.workers.config.AppProperties
import com.lab.workers.persistence.OutboxEventRepository
import io.awspring.cloud.sns.core.SnsTemplate
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Publica eventos gravados na outbox para o SNS.
 * Separado da transação de criação do pedido: at-least-once na mensageria;
 * idempotência no worker compensa republicações.
 */
@Component
class OutboxPublisher(
    private val outboxEventRepository: OutboxEventRepository,
    private val snsTemplate: SnsTemplate,
    private val appProperties: AppProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${app.outbox.publish-interval-ms}")
    @Transactional
    fun publishPending() {
        val batch =
            outboxEventRepository.findUnpublished(
                PageRequest.of(0, appProperties.outbox.batchSize),
            )
        if (batch.isEmpty()) {
            return
        }

        val topic = appProperties.sns.orderEventsTopic
        batch.forEach { event ->
            snsTemplate.sendNotification(topic, event.payload, event.eventType)
            event.publishedAt = Instant.now()
            log.info("Outbox published event {} for aggregate {}", event.id, event.aggregateId)
        }
    }
}
