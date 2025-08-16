package com.gasolinerajsm.raffleservice.adapter.out.seed

interface SeedProvider {
    fun getSeed(blockHeight: Long): String
}
