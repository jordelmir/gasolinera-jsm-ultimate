package com.gasolinerajsm.authservice.repository

import com.gasolinerajsm.authservice.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface UserRepository : JpaRepository<User, java.util.UUID> {
    fun findByPhoneNumber(phoneNumber: String): User?
}
