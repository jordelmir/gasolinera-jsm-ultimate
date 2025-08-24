package com.gasolinerajsm.raffleservice.exception

/**
 * Exception thrown when a raffle is not found
 */
class RaffleNotFoundException(message: String) : RuntimeException(message)

/**
 * Exception thrown when raffle validation fails
 */
class RaffleValidationException(message: String, val errors: List<String> = emptyList()) : RuntimeException(message)

/**
 * Exception thrown when raffle operation is not allowed
 */
class RaffleOperationException(message: String) : RuntimeException(message)

/**
 * Exception thrown when participant operation fails
 */
class ParticipantException(message: String) : RuntimeException(message)

/**
 * Exception thrown when external seed cannot be obtained
 */
class ExternalSeedException(message: String) : RuntimeException(message)