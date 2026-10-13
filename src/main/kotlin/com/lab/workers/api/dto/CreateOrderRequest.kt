package com.lab.workers.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class CreateOrderRequest(
    @field:Email
    val customerEmail: String,
    @field:NotBlank
    val productSku: String,
    @field:Min(1)
    val quantity: Int,
    /**
     * Opcional: simula falhas no mock externo para estudar retries/DLQ.
     * Valores: none | transient | permanent
     */
    val simulateExternalFailure: String? = null,
)
