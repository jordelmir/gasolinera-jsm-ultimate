package com.gasolinerajsm.authservice.service

import com.gasolinerajsm.authservice.model.User
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

    fun findOrCreateUser(phoneNumber: String): User {
        return userRepository.findByPhoneNumber(phoneNumber) ?: userRepository.save(User(phoneNumber = phoneNumber))
    }

    fun findUserById(userId: UUID): User? {
        return userRepository.findById(userId).orElse(null)
    }
}
