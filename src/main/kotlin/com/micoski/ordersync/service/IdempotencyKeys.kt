package com.micoski.ordersync.service

import java.util.UUID

object IdempotencyKeys {
    fun externalSubmit(orderId: UUID): String = "order-submit:$orderId"
}
