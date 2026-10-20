package com.micoski.ordersync.persistence

import com.micoski.ordersync.domain.ProcessedOperationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProcessedOperationRepository : JpaRepository<ProcessedOperationEntity, String>
