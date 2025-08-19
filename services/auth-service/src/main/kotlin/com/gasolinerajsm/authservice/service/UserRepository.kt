package com.gasolinerajsm.authservice.service

import com.gasolinerajsm.authservice.model.User
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository
import org.slf4j.LoggerFactory

@Repository
class UserRepository {

    private val logger = LoggerFactory.getLogger(UserRepository::class.java)

    // Mock data for demonstration
    private val users = mutableMapOf(
        "user-placeholder-id" to User(id = "user-placeholder-id", phone = "1234567890", roles = listOf("USER")),
        "admin-placeholder-id" to User(id = "admin-placeholder-id", phone = "0987654321", roles = listOf("ADMIN"))
    )

    @Cacheable(value = "users", key = "#id")
    fun findById(id: String): User? {
        logger.info("Fetching user with ID: {} from database (mock)", id)
        // Simulate database call delay
        Thread.sleep(1000)
        return users[id]
    }

    fun save(user: User): User {
        logger.info("Saving user with ID: {} to database (mock)", user.id)
        users[user.id] = user
        return user
    }
}