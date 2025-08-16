package com.gasolinerajsm.apigateway.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<Map<String, Any>> {
        logger.error("An unexpected error occurred: {}", ex.message, ex)
        val body = mapOf(
            "timestamp" to LocalDateTime.now(),
            "status" to HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "error" to "Internal Server Error",
            "message" to "An unexpected error occurred. Please try again later."
        )
        return ResponseEntity(body, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(InvalidRequestException::class)
    fun handleSpecificApiException(ex: InvalidRequestException): ResponseEntity<Map<String, Any>> {
        logger.warn("Invalid request: {}", ex.message)
        val body = mapOf(
            "timestamp" to LocalDateTime.now(),
            "status" to HttpStatus.BAD_REQUEST.value(),
            "error" to "Bad Request",
            "message" to ex.message
        )
        return ResponseEntity(body, HttpStatus.BAD_REQUEST)
    }
}
