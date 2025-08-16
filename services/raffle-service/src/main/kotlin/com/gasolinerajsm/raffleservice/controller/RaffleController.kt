
package com.gasolinerajsm.raffleservice.controller

import com.gasolinerajsm.raffleservice.service.RaffleService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/raffles")
class RaffleController(private val raffleService: RaffleService) {

    @PostMapping
    fun createRaffle(): String {
        return raffleService.createRaffleForCurrentPeriod()
    }

    @PostMapping("/{id}/draw")
    fun drawWinner(@PathVariable id: String, @RequestBody request: DrawRequest): RaffleWinner {
        return raffleService.drawWinner(java.util.UUID.fromString(id), request.seed_value)
    }
}

data class DrawRequest(val seed_value: String?)
data class RaffleWinner(val user_id: String, val point_id: String)
