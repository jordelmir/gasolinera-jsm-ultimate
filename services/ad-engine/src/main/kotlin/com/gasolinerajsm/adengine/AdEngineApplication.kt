package com.gasolinerajsm.adengine

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
class AdEngineApplication

fun main(args: Array<String>) {
    runApplication<AdEngineApplication>(*args)
}
