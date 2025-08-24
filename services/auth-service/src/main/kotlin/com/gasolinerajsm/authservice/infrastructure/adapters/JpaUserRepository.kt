package com.gasolinerajsm.authservice.infrastructure.adapters

import com.gasolinerajsm.authservice.domain.PhoneNumber
import com.gasolinerajsm.authservice.domain.User
import com.gasolinerajsm.authservice.domain.UserId
import com.gasolinerajsm.authservice.domain.ports.UserRepository
import com.gasolinerajsm.authservice.infrastructure.entities.UserEntity
import com.gasolinerajsm.authservice.infrastructure.repositories.JpaUserEntityRepository
import org.springframework.stereotype.Repository

/**
 * JPA adapter implementation of UserRepository port.
 * This translates between domain objects and JPA entities.
 */
@Repository
class JpaUserRepository(
    private val jpaRepository: JpaUserEntityRepository
) : UserRepository {

    override fun findById(id: UserId): User? {
        return jpaRepository.findById(id.value)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByPhoneNumber(phoneNumber: PhoneNumber): User? {
        return jpaRepository.findByPhoneNumber(phoneNumber.value)
            ?.toDomain()
    }

    override fun save(user: User): User {
        val entity = UserEntity.fromDomain(user)
        val savedEntity = jpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun existsByPhoneNumber(phoneNumber: PhoneNumber): Boolean {
        return jpaRepository.existsByPhoneNumber(phoneNumber.value)
    }
}