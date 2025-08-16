
package com.gasolinerajsm.stationservice.controller

import com.gasolinerajsm.stationservice.service.StationService
import org.springframework.web.bind.annotation.* 

@RestController
@RequestMapping("/stations")
class StationController(private val stationService: StationService) {

    @GetMapping("/{id}")
    fun getStationById(@PathVariable id: String): StationDto {
        return stationService.findById(id)
    }

    // A real implementation would use PostGIS for efficient geo-queries
    @GetMapping
    fun findStations(@RequestParam(required = false) near: String?): List<StationDto> {
        return stationService.findAll()
    }
}

data class StationDto(val id: String, val name: String, val location: String)
