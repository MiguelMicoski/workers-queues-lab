package com.micoski.ordersync.persistence

import com.micoski.ordersync.domain.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrderRepository : JpaRepository<OrderEntity, UUID>
