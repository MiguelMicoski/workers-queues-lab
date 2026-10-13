package com.lab.workers.api

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.ConcurrentHashMap

/**
 * Simula um ERP/parceiro com suporte a Idempotency-Key.
 */
@RestController
@RequestMapping("/mock-external")
class MockExternalOrderController {
    private val store = ConcurrentHashMap<String, String>()

    @PostMapping("/orders")
    fun submit(
        @RequestHeader("Idempotency-Key") idempotencyKey: String,
        @RequestHeader(name = "X-Simulate-Failure", defaultValue = "none") simulateFailure: String,
        @RequestBody body: Map<String, Any?>,
    ): Map<String, String> {
        store[idempotencyKey]?.let { previous ->
            return mapOf(
                "status" to "duplicate_ignored",
                "externalReference" to previous,
                "idempotencyKey" to idempotencyKey,
            )
        }

        when (simulateFailure.lowercase()) {
            "transient" ->
                throw ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Simulated transient outage",
                )
            "permanent" ->
                throw ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Simulated permanent rejection",
                )
        }

        val reference = "EXT-${idempotencyKey.hashCode().toUInt()}"
        store[idempotencyKey] = reference
        return mapOf(
            "status" to "accepted",
            "externalReference" to reference,
            "idempotencyKey" to idempotencyKey,
            "payloadEcho" to body.toString(),
        )
    }
}
