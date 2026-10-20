package com.micoski.ordersync.partner

import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.ConcurrentHashMap

@Profile("local")
@RestController
@RequestMapping("/partner")
class LocalPartnerWebhook {
    private val acceptedKeys = ConcurrentHashMap.newKeySet<String>()

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun acceptOrder(
        @RequestHeader("Idempotency-Key") idempotencyKey: String,
        @RequestBody body: Map<String, Any?>,
    ): Map<String, String> {
        val duplicate = !acceptedKeys.add(idempotencyKey)
        return mapOf(
            "idempotencyKey" to idempotencyKey,
            "status" to if (duplicate) "ALREADY_ACCEPTED" else "ACCEPTED",
            "orderId" to (body["orderId"]?.toString() ?: ""),
        )
    }
}
