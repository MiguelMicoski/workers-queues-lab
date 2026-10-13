package com.lab.workers.persistence

import com.lab.workers.domain.ProcessedOperationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProcessedOperationRepository : JpaRepository<ProcessedOperationEntity, String>
