package com.gasolinerajsm.coupon.service

import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.*

@Service
class TokenGenerator {

    private val secureRandom = SecureRandom()
    private val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    fun generateUniqueToken(): String {
        // Generar token único de 12 caracteres
        val token = StringBuilder()
        repeat(12) {
            token.append(characters[secureRandom.nextInt(characters.length)])
        }

        // Agregar timestamp para garantizar unicidad
        val timestamp = System.currentTimeMillis().toString(36).uppercase()

        return "${token}-${timestamp}"
    }

    fun generateRaffleToken(): String {
        // Token especial para sorteos con formato diferente
        val uuid = UUID.randomUUID().toString().replace("-", "").uppercase()
        return "RAFFLE-${uuid.take(16)}"
    }

    fun validateTokenFormat(token: String): Boolean {
        return token.matches(Regex("^[A-Z0-9]{12}-[A-Z0-9]+$"))
    }
}