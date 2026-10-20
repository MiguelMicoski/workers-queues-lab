package com.micoski.ordersync.outbox

import com.micoski.ordersync.config.AppProperties
import com.micoski.ordersync.persistence.OutboxEventRepository
import io.awspring.cloud.sns.core.SnsTemplate
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

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
            log.info("Published outbox event {} for order {}", event.id, event.aggregateId)
        }
    }
}
