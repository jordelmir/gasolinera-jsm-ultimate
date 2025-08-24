package com.gasolinerajsm.authservice.infrastructure.repositories

import com.gasolinerajsm.authservice.infrastructure.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Spring Data JPA repository for UserEntity
 */
@Repository
interface JpaUserEntityRepository : JpaRepository<UserEntity, UUID> {

    fun findByPhoneNumber(phoneNumber: String): UserEntity?

    fun existsByPhoneNumber(phoneNumber: String): Boolean
}