package com.gasolinerajsm.raffleservice.controller

import com.gasolinerajsm.raffleservice.model.Raffle
import com.gasolinerajsm.raffleservice.model.RaffleWinner
import com.gasolinerajsm.raffleservice.service.RaffleService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/raffles")
class RaffleController(private val raffleService: RaffleService) {

    @PostMapping("/{period}/close")
    @ResponseStatus(HttpStatus.OK)
    fun closeRafflePeriod(@PathVariable period: String): Raffle {
        return raffleService.closeRafflePeriod(period)
    }

    @PostMapping("/{id}/draw")
    @ResponseStatus(HttpStatus.OK)
    fun executeRaffleDraw(@PathVariable id: Long): RaffleWinner {
        return raffleService.executeRaffleDraw(id)
    }

    // TODO: Add GET endpoints for listing raffles and winners
}