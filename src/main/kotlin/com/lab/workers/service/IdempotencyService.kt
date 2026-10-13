package com.lab.workers.service

import com.lab.workers.domain.ProcessedOperationEntity
import com.lab.workers.persistence.ProcessedOperationRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class IdempotencyService(
    private val processedOperationRepository: ProcessedOperationRepository,
) {
    fun alreadyProcessed(idempotencyKey: String): Boolean =
        processedOperationRepository.existsById(idempotencyKey)

    @Transactional
    fun recordSuccess(idempotencyKey: String, orderId: UUID) {
        if (processedOperationRepository.existsById(idempotencyKey)) {
            return
        }
        try {
            processedOperationRepository.save(
                ProcessedOperationEntity(
                    idempotencyKey = idempotencyKey,
                    orderId = orderId,
                    outcome = "SUCCESS",
                ),
            )
        } catch (_: DataIntegrityViolationException) {
            // Corrida entre consumidores paralelos: outra instância já registrou a mesma chave.
        }
    }
}
