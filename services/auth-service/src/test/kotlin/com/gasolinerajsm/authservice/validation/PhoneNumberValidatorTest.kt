package com.gasolinerajsm.authservice.validation

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class PhoneNumberValidatorTest {

    private val validator = PhoneNumberValidator()

    @Test
    fun `should validate E164 international format`() {
        val validE164Numbers = listOf(
            "+1234567890",
            "+12345678901",
            "+123456789012345", // Max 15 digits
            "+50688888888",
            "+19876543210"
        )

        validE164Numbers.forEach { number ->
            val result = validator.validate(number)
            assertTrue(result.isValid, "Should validate $number as valid E.164")
            assertEquals(number, result.normalizedNumber)
            assertNull(result.errorMessage)
        }
    }

    @Test
    fun `should reject invalid E164 format`() {
        val invalidE164Numbers = listOf(
            "+0123456789", // Cannot start with 0
            "+1234567890123456", // Too long (16 digits)
            "+12", // Too short
            "1234567890", // Missing +
            "+", // Just plus sign
            "+abc1234567" // Contains letters
        )

        invalidE164Numbers.forEach { number ->
            val result = validator.validate(number)
            assertFalse(result.isValid, "Should reject $number as invalid E.164")
            assertNull(result.normalizedNumber)
            assertNotNull(result.errorMessage)
        }
    }

    @Test
    fun `should validate Costa Rican landline with dash`() {
        val validNumbers = listOf(
            "2222-3333",
            "3444-5555",
            "4666-7777",
            "5888-9999",
            "6111-2222",
            "7333-4444"
        )

        validNumbers.forEach { number ->
            val result = validator.validate(number)
            assertTrue(result.isValid, "Should validate $number as valid Costa Rican landline")
            assertEquals("+506${number.replace("-", "")}", result.normalizedNumber)
            assertNull(result.errorMessage)
        }
    }

    @Test
    fun `should validate Costa Rican landline without dash`() {
        val validNumbers = listOf(
            "22223333",
            "34445555",
            "46667777",
            "58889999",
            "61112222",
            "73334444"
        )

        validNumbers.forEach { number ->
            val result = validator.validate(number)
            assertTrue(result.isValid, "Should validate $number as valid Costa Rican landline")
            assertEquals("+506$number", result.normalizedNumber)
            assertNull(result.errorMessage)
        }
    }

    @Test
    fun `should validate Costa Rican mobile with dash`() {
        val validNumbers = listOf(
            "6111-2222",
            "7333-4444",
            "8555-6666",
            "9777-8888"
        )

        validNumbers.forEach { number ->
            val result = validator.validate(number)
            assertTrue(result.isValid, "Should validate $number as valid Costa Rican mobile")
            assertEquals("+506${number.replace("-", "")}", result.normalizedNumber)
            assertNull(result.errorMessage)
        }
    }

    @Test
    fun `should validate Costa Rican mobile without dash`() {
        val validNumbers = listOf(
            "61112222",
            "73334444",
            "85556666",
            "97778888"
        )

        validNumbers.forEach { number ->
            val result = validator.validate(number)
            assertTrue(result.isValid, "Should validate $number as valid Costa Rican mobile")
            assertEquals("+506$number", result.normalizedNumber)
            assertNull(result.errorMessage)
        }
    }

    @Test
    fun `should reject invalid Costa Rican numbers`() {
        val invalidNumbers = listOf(
            "1111-2222", // Cannot start with 1
            "0333-4444", // Cannot start with 0
            "2222-333", // Wrong format (3 digits after dash)
            "22223", // Too short
            "222233334", // Too long
            "abcd-efgh", // Contains letters
            "2222-", // Missing digits after dash
            "-2222" // Missing digits before dash
        )

        invalidNumbers.forEach { number ->
            val result = validator.validate(number)
            assertFalse(result.isValid, "Should reject $number as invalid Costa Rican number")
            assertNull(result.normalizedNumber)
            assertNotNull(result.errorMessage)
        }
    }

    @Test
    fun `should handle empty and blank strings`() {
        val emptyInputs = listOf("", " ", "  ", "\t", "\n")

        emptyInputs.forEach { input ->
            val result = validator.validate(input)
            assertFalse(result.isValid, "Should reject empty/blank input: '$input'")
            assertNull(result.normalizedNumber)
            assertEquals("Phone number cannot be empty", result.errorMessage)
        }
    }

    @Test
    fun `should trim whitespace before validation`() {
        val numbersWithWhitespace = mapOf(
            " +1234567890 " to "+1234567890",
            "\t2222-3333\n" to "+50622223333",
            "  61112222  " to "+50661112222"
        )

        numbersWithWhitespace.forEach { (input, expected) ->
            val result = validator.validate(input)
            assertTrue(result.isValid, "Should validate trimmed input: '$input'")
            assertEquals(expected, result.normalizedNumber)
            assertNull(result.errorMessage)
        }
    }

    @Test
    fun `normalize should return normalized number for valid input`() {
        val testCases = mapOf(
            "+1234567890" to "+1234567890",
            "2222-3333" to "+50622223333",
            "61112222" to "+50661112222",
            "8555-6666" to "+50685556666"
        )

        testCases.forEach { (input, expected) ->
            val result = validator.normalize(input)
            assertEquals(expected, result, "Should normalize $input to $expected")
        }
    }

    @Test
    fun `normalize should return null for invalid input`() {
        val invalidInputs = listOf(
            "invalid",
            "1111-2222",
            "+0123456789",
            "",
            "abc"
        )

        invalidInputs.forEach { input ->
            val result = validator.normalize(input)
            assertNull(result, "Should return null for invalid input: $input")
        }
    }

    @Test
    fun `isValidFormat should return correct boolean`() {
        val validNumbers = listOf(
            "+1234567890",
            "2222-3333",
            "61112222",
            "8555-6666"
        )

        val invalidNumbers = listOf(
            "invalid",
            "1111-2222",
            "+0123456789",
            ""
        )

        validNumbers.forEach { number ->
            assertTrue(validator.isValidFormat(number), "Should return true for valid: $number")
        }

        invalidNumbers.forEach { number ->
            assertFalse(validator.isValidFormat(number), "Should return false for invalid: $number")
        }
    }
}