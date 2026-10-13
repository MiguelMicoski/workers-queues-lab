package com.lab.workers.persistence

import com.lab.workers.domain.DlqRecordEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DlqRecordRepository : JpaRepository<DlqRecordEntity, UUID>
