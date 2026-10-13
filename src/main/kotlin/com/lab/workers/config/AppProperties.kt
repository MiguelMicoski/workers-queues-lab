package com.lab.workers.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val sns: SnsProperties,
    val sqs: SqsProperties,
    val outbox: OutboxProperties,
    val worker: WorkerProperties,
    val external: ExternalProperties,
) {
    data class SnsProperties(val orderEventsTopic: String)
    data class SqsProperties(
        val orderProcessingQueue: String,
        val orderProcessingDlq: String,
    )
    data class OutboxProperties(
        val publishIntervalMs: Long,
        val batchSize: Int,
    )
    data class WorkerProperties(val maxAttemptsBeforePermanent: Int)
    data class ExternalProperties(val baseUrl: String)
}
