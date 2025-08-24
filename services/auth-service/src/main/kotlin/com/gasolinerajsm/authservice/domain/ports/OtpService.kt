package com.gasolinerajsm.authservice.domain.ports

import com.gasolinerajsm.authservice.domain.PhoneNumber

/**
 * Port (interface) for OTP operations.
 * This abstracts the OTP storage and verification logic.
 */
interface OtpService {

    /**
     * Generate and store an OTP for the given phone number
     */
    fun generateAndStore(phoneNumber: PhoneNumber): String

    /**
     * Verify if the provided OTP is valid for the phone number
     */
    fun verify(phoneNumber: PhoneNumber, otp: String): Boolean

    /**
     * Remove the OTP for the given phone number
     */
    fun remove(phoneNumber: PhoneNumber)
}