package com.lab.workers.api

import com.lab.workers.domain.DlqRecordEntity
import com.lab.workers.domain.OrderEntity
import com.lab.workers.persistence.DlqRecordRepository
import com.lab.workers.persistence.OrderRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/lab")
class LabDiagnosticsController(
    private val orderRepository: OrderRepository,
    private val dlqRecordRepository: DlqRecordRepository,
) {
    @GetMapping("/orders")
    fun orders(): List<OrderEntity> = orderRepository.findAll()

    @GetMapping("/dlq-records")
    fun dlqRecords(): List<DlqRecordEntity> = dlqRecordRepository.findAll()
}
