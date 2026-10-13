package com.lab.workers.api

import com.lab.workers.api.dto.CreateOrderRequest
import com.lab.workers.api.dto.OrderResponse
import com.lab.workers.service.OrderService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun create(@Valid @RequestBody request: CreateOrderRequest): OrderResponse =
        orderService.createOrder(request)
}
