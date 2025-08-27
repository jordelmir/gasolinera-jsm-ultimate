package com.gasolinerajsm.authservice.validation

import org.springframework.stereotype.Component
import java.util.regex.Pattern

/**
 * Validator for phone number formats supporting international E.164 format
 * and Costa Rican local formats.
 */
@Component
class PhoneNumberValidator {

    companion object {
        // E.164 international format: +[country code][number] (max 15 digits total)
        private val E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$")

        // Costa Rican formats:
        // - 8888-8888 (landline with dash)
        // - 88888888 (8 digits without dash)
        // - 6888-8888 (mobile with dash)
        // - 68888888 (8 digits mobile without dash)
        private val COSTA_RICA_LANDLINE_DASH = Pattern.compile("^[2-7]\\d{3}-\\d{4}$")
        private val COSTA_RICA_LANDLINE_NO_DASH = Pattern.compile("^[2-7]\\d{7}$")
        private val COSTA_RICA_MOBILE_DASH = Pattern.compile("^[6-9]\\d{3}-\\d{4}$")
        private val COSTA_RICA_MOBILE_NO_DASH = Pattern.compile("^[6-9]\\d{7}$")

        private const val COSTA_RICA_COUNTRY_CODE = "+506"
    }

    /**
     * Validates if a phone number is in a supported format.
     *
     * @param phoneNumber The phone number to validate
     * @return ValidationResult containing validation status and normalized number
     */
    fun validate(phoneNumber: String): ValidationResult {
        if (phoneNumber.isBlank()) {
            return ValidationResult(false, null, "Phone number cannot be empty")
        }

        val trimmed = phoneNumber.trim()

        // Check E.164 format first
        if (E164_PATTERN.matcher(trimmed).matches()) {
            return ValidationResult(true, trimmed, null)
        }

        // Check Costa Rican formats
        val normalizedCostaRica = normalizeCostaRicanNumber(trimmed)
        if (normalizedCostaRica != null) {
            return ValidationResult(true, normalizedCostaRica, null)
        }

        return ValidationResult(
            false,
            null,
            "Invalid phone number format. Supported formats: E.164 (+1234567890) or Costa Rican (8888-8888, 88888888)"
        )
    }

    /**
     * Normalizes a phone number to E.164 format.
     *
     * @param phoneNumber The phone number to normalize
     * @return Normalized phone number in E.164 format, or null if invalid
     */
    fun normalize(phoneNumber: String): String? {
        val validationResult = validate(phoneNumber)
        return if (validationResult.isValid) validationResult.normalizedNumber else null
    }

    /**
     * Checks if a phone number is in a valid format without normalization.
     *
     * @param phoneNumber The phone number to check
     * @return true if valid format, false otherwise
     */
    fun isValidFormat(phoneNumber: String): Boolean {
        return validate(phoneNumber).isValid
    }

    /**
     * Normalizes Costa Rican phone numbers to E.164 format.
     *
     * @param phoneNumber The Costa Rican phone number
     * @return Normalized E.164 format or null if invalid
     */
    private fun normalizeCostaRicanNumber(phoneNumber: String): String? {
        val cleaned = phoneNumber.replace("-", "")

        return when {
            COSTA_RICA_LANDLINE_DASH.matcher(phoneNumber).matches() ||
            COSTA_RICA_LANDLINE_NO_DASH.matcher(cleaned).matches() ||
            COSTA_RICA_MOBILE_DASH.matcher(phoneNumber).matches() ||
            COSTA_RICA_MOBILE_NO_DASH.matcher(cleaned).matches() -> {
                "$COSTA_RICA_COUNTRY_CODE$cleaned"
            }
            else -> null
        }
    }
}

/**
 * Result of phone number validation.
 */
data class ValidationResult(
    val isValid: Boolean,
    val normalizedNumber: String?,
    val errorMessage: String?
)