package com.lab.workers.external

import com.lab.workers.config.AppProperties
import com.lab.workers.messaging.OrderSubmittedEvent
import com.lab.workers.worker.PermanentProcessingException
import com.lab.workers.worker.TransientProcessingException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Component
class ExternalOrderClient(
    restClientBuilder: RestClient.Builder,
    appProperties: AppProperties,
) {
    private val restClient =
        restClientBuilder
            .baseUrl(appProperties.external.baseUrl)
            .build()

    fun submitOrder(event: OrderSubmittedEvent) {
        try {
            restClient
                .post()
                .uri("/orders")
                .header("Idempotency-Key", event.idempotencyKey)
                .header("X-Simulate-Failure", event.simulateExternalFailure ?: "none")
                .body(
                    mapOf(
                        "orderId" to event.orderId.toString(),
                        "productSku" to event.productSku,
                        "quantity" to event.quantity,
                    ),
                )
                .retrieve()
                .toBodilessEntity()
        } catch (ex: RestClientResponseException) {
            when (ex.statusCode) {
                HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.BAD_GATEWAY, HttpStatus.GATEWAY_TIMEOUT ->
                    throw TransientProcessingException("External transient: ${ex.statusCode}", ex)
                else -> throw PermanentProcessingException("External permanent: ${ex.statusCode}", ex)
            }
        } catch (ex: Exception) {
            throw TransientProcessingException("External call failed", ex)
        }
    }
}
