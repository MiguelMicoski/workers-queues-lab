package com.micoski.ordersync.persistence

import com.micoski.ordersync.domain.DlqRecordEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DlqRecordRepository : JpaRepository<DlqRecordEntity, UUID>
