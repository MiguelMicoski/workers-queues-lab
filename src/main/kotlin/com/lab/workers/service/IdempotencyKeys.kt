package com.lab.workers.service

import java.util.UUID

object IdempotencyKeys {
    /** Chave estável por pedido para envio ao sistema externo. */
    fun externalSubmit(orderId: UUID): String = "order-submit:$orderId"
}
