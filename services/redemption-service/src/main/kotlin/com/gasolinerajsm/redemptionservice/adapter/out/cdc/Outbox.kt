package com.gasolinerajsm.redemptionservice.adapter.out.cdc

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "outbox")
data class Outbox(
    @Id
    val id: UUID = UUID.randomUUID(),
    val aggregateType: String,
    val aggregateId: String,
    val eventType: String,
    val payload: String,
    val createdAt: Instant = Instant.now()
)
