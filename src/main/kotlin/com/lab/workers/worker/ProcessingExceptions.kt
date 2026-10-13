package com.lab.workers.worker

class TransientProcessingException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class PermanentProcessingException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
