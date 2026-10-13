package com.lab.workers.persistence

import com.lab.workers.domain.OutboxEventEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface OutboxEventRepository : JpaRepository<OutboxEventEntity, UUID> {
    @Query(
        """
        SELECT e FROM OutboxEventEntity e
        WHERE e.publishedAt IS NULL
        ORDER BY e.createdAt ASC
        """,
    )
    fun findUnpublished(pageable: Pageable): List<OutboxEventEntity>
}
