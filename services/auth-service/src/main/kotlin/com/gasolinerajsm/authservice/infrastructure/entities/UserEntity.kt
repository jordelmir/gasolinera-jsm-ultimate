package com.gasolinerajsm.authservice.infrastructure.entities

import com.gasolinerajsm.authservice.domain.PhoneNumber
import com.gasolinerajsm.authservice.domain.Role
import com.gasolinerajsm.authservice.domain.User
import com.gasolinerajsm.authservice.domain.UserId
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

/**
 * JPA entity for User persistence.
 * This is separate from the domain User to maintain clean architecture.
 */
@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    val id: UUID,

    @Column(name = "phone_number", unique = true, nullable = false)
    val phoneNumber: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "role")
    val roles: Set<Role>,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime
) {
    /**
     * Convert JPA entity to domain object
     */
    fun toDomain(): User {
        return User(
            id = UserId(id),
            phoneNumber = PhoneNumber(phoneNumber),
            roles = roles,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        /**
         * Create JPA entity from domain object
         */
        fun fromDomain(user: User): UserEntity {
            return UserEntity(
                id = user.id.value,
                phoneNumber = user.phoneNumber.value,
                roles = user.roles,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}