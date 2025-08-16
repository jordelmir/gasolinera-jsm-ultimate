
package com.gasolinerajsm.raffleservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class RaffleServiceApplication

fun main(args: Array<String>) {
    runApplication<RaffleServiceApplication>(*args)
}
