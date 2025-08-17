package com.gasolinerajsm.stationservice.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "stations")
data class Station(
    @Id
    var id: String,
    var name: String,
    var latitude: Double,
    var longitude: Double,
    var status: String
)
