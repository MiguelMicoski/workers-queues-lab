package com.micoski.ordersync.external

import com.micoski.ordersync.config.AppProperties
import com.micoski.ordersync.messaging.OrderSubmittedEvent
import com.micoski.ordersync.worker.PermanentProcessingException
import com.micoski.ordersync.worker.TransientProcessingException
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
            .baseUrl(appProperties.partner.baseUrl)
            .build()

    fun submitOrder(event: OrderSubmittedEvent) {
        try {
            restClient
                .post()
                .uri("/orders")
                .header("Idempotency-Key", event.idempotencyKey)
                .body(
                    mapOf(
                        "orderId" to event.orderId.toString(),
                        "customerEmail" to event.customerEmail,
                        "productSku" to event.productSku,
                        "quantity" to event.quantity,
                    ),
                )
                .retrieve()
                .toBodilessEntity()
        } catch (ex: RestClientResponseException) {
            when (ex.statusCode) {
                HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.BAD_GATEWAY, HttpStatus.GATEWAY_TIMEOUT ->
                    throw TransientProcessingException("Partner transient error: ${ex.statusCode}", ex)
                else -> throw PermanentProcessingException("Partner permanent error: ${ex.statusCode}", ex)
            }
        } catch (ex: Exception) {
            throw TransientProcessingException("Partner request failed", ex)
        }
    }
}
