package com.micoski.ordersync.api

import com.micoski.ordersync.api.dto.CreateOrderRequest
import com.micoski.ordersync.api.dto.OrderResponse
import com.micoski.ordersync.service.OrderService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun create(@Valid @RequestBody request: CreateOrderRequest): OrderResponse =
        orderService.createOrder(request)

    @GetMapping("/{orderId}")
    fun get(@PathVariable orderId: UUID): OrderResponse =
        try {
            orderService.getOrder(orderId)
        } catch (_: NoSuchElementException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")
        }
}
