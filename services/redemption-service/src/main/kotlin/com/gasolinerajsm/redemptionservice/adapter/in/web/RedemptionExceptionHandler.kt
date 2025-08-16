package com.gasolinerajsm.redemptionservice.adapter.in.web

import com.gasolinerajsm.redemptionservice.exception.InvalidQrException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.LocalDateTime

@ControllerAdvice
class RedemptionExceptionHandler {

    private val logger = LoggerFactory.getLogger(RedemptionExceptionHandler::class.java)

    @ExceptionHandler(InvalidQrException::class)
    fun handleInvalidQrException(ex: InvalidQrException): ResponseEntity<Map<String, Any>> {
        logger.warn("Invalid QR request: {}", ex.message)
        val body = mapOf(
            "timestamp" to LocalDateTime.now(),
            "status" to HttpStatus.BAD_REQUEST.value(),
            "error" to "Bad Request",
            "message" to ex.message
        )
        return ResponseEntity(body, HttpStatus.BAD_REQUEST)
    }

    // You might want to add a generic exception handler here as well
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<Map<String, Any>> {
        logger.error("An unexpected error occurred in Redemption Service: {}", ex.message, ex)
        val body = mapOf(
            "timestamp" to LocalDateTime.now(),
            "status" to HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "error" to "Internal Server Error",
            "message" to "An unexpected error occurred. Please try again later."
        )
        return ResponseEntity(body, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
