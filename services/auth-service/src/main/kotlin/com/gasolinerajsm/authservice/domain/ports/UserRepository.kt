package com.gasolinerajsm.authservice.domain.ports

import com.gasolinerajsm.authservice.domain.PhoneNumber
import com.gasolinerajsm.authservice.domain.User
import com.gasolinerajsm.authservice.domain.UserId

/**
 * Port (interface) for user repository operations.
 * This follows the hexagonal architecture pattern where the domain defines
 * the contract that infrastructure adapters must implement.
 */
interface UserRepository {

    /**
     * Find a user by their unique ID
     */
    fun findById(id: UserId): User?

    /**
     * Find a user by their phone number
     */
    fun findByPhoneNumber(phoneNumber: PhoneNumber): User?

    /**
     * Save a user (create or update)
     */
    fun save(user: User): User

    /**
     * Check if a user exists with the given phone number
     */
    fun existsByPhoneNumber(phoneNumber: PhoneNumber): Boolean
}