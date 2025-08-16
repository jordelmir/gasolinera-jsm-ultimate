
package com.gasolinerajsm.raffleservice.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RaffleScheduler(private val raffleService: RaffleService) {

    @Scheduled(cron = "0 0 0 * * MON")
    fun createWeeklyRaffle() {
        raffleService.createRaffleForCurrentPeriod(40000)
    }

    @Scheduled(cron = "0 0 0 14 2 ?")
    fun createCarRaffle() {
        raffleService.createCarRaffle()
    }
}
