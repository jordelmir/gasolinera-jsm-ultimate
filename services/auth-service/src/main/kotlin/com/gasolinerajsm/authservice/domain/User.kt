package com.gasolinerajsm.authservice.domain

import java.time.LocalDateTime
import java.util.*

/**
 * Domain entity representing a user in the system.
 * This is the core business entity that contains all business rules and invariants.
 */
data class User(
    val id: UserId,
    val phoneNumber: PhoneNumber,
    val roles: Set<Role>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        /**
         * Factory method to create a new user with default values.
         */
        fun create(phoneNumber: PhoneNumber): User {
            val now = LocalDateTime.now()
            return User(
                id = UserId.generate(),
                phoneNumber = phoneNumber,
                roles = setOf(Role.USER),
                createdAt = now,
                updatedAt = now
            )
        }
    }

    /**
     * Business rule: Check if user has a specific role
     */
    fun hasRole(role: Role): Boolean = roles.contains(role)

    /**
     * Business rule: Check if user is admin
     */
    fun isAdmin(): Boolean = hasRole(Role.ADMIN)

    /**
     * Business rule: Check if user is advertiser
     */
    fun isAdvertiser(): Boolean = hasRole(Role.ADVERTISER)
}

/**
 * Value object representing a user ID
 */
@JvmInline
value class UserId(val value: UUID) {
    companion object {
        fun generate(): UserId = UserId(UUID.randomUUID())
        fun from(value: String): UserId = UserId(UUID.fromString(value))
    }

    override fun toString(): String = value.toString()
}

/**
 * Value object representing a phone number with validation
 */
@JvmInline
value class PhoneNumber(val value: String) {
    init {
        require(value.isNotBlank()) { "Phone number cannot be blank" }
        require(isValidPhoneNumber(value)) { "Invalid phone number format: $value" }
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        // Basic validation - can be enhanced based on requirements
        return phone.matches(Regex("^\\+?[1-9]\\d{7,14}$"))
    }
}

/**
 * Enum representing user roles in the system
 */
enum class Role {
    USER,
    ADMIN,
    ADVERTISER
}