package com.micoski.ordersync.api.dto

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
)
