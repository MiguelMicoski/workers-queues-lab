package com.micoski.ordersync.domain

enum class OrderStatus {
    RECEIVED,
    PROCESSING,
    SYNCED_EXTERNAL,
    FAILED,
}
