package com.gasolinerajsm.coupon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class CouponServiceApplication

fun main(args: Array<String>) {
    runApplication<CouponServiceApplication>(*args)
}