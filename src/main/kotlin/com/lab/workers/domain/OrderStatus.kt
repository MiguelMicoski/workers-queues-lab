package com.lab.workers.domain

enum class OrderStatus {
    RECEIVED,
    PROCESSING,
    SYNCED_EXTERNAL,
    FAILED,
}
