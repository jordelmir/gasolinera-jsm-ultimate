package com.gasolinerajsm.authservice.controller

import com.gasolinerajsm.authservice.application.AuthenticationUseCase
import com.gasolinerajsm.authservice.application.RequestOtpResult
import com.gasolinerajsm.authservice.application.VerifyOtpResult
import com.gasolinerajsm.authservice.domain.PhoneNumber
import com.gasolinerajsm.authservice.dto.AdminLoginRequest
import com.gasolinerajsm.authservice.dto.OtpRequest
import com.gasolinerajsm.authservice.dto.OtpVerifyRequest
import com.gasolinerajsm.authservice.dto.TokenResponse
import com.gasolinerajsm.authservice.service.AuthService
import com.gasolinerajsm.authservice.service.JwtService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for authentication endpoints.
 * This controller acts as an adapter in the hexagonal architecture,
 * translating HTTP requests to use case calls.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
class AuthController(
    private val authenticationUseCase: AuthenticationUseCase,
    private val authService: AuthService,
    private val jwtService: JwtService, // TODO: Remove when admin login is refactored
    private val env: Environment
) {

    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @Operation(
        summary = "Request OTP",
        description = "Generates and sends a 6-digit OTP code to the provided phone number"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "OTP sent successfully"
        ),
        ApiResponse(
            responseCode = "400",
            description = "Invalid phone number format",
            content = [Content(schema = Schema(implementation = Map::class))]
        )
    ])
    @PostMapping("/otp/request", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun requestOtp(
        @Parameter(description = "Phone number to send OTP to", required = true)
        @Valid @RequestBody request: OtpRequest
    ): ResponseEntity<Any> {
        return try {
            val phoneNumber = PhoneNumber(request.phone)
            val result = authenticationUseCase.requestOtp(phoneNumber)

            when (result) {
                is RequestOtpResult.Success -> ResponseEntity.ok().build()
                is RequestOtpResult.Error -> ResponseEntity.badRequest()
                    .body(mapOf("message" to result.message))
            }
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid phone number format: {}", request.phone)
            ResponseEntity.badRequest()
                .body(mapOf("message" to "Invalid phone number format"))
        }
    }

    @Operation(
        summary = "Verify OTP",
        description = "Verifies the OTP code and returns JWT tokens for authentication"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "OTP verified successfully, tokens returned",
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = TokenResponse::class)
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Invalid or expired OTP",
            content = [Content(schema = Schema(implementation = Map::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "Invalid request format",
            content = [Content(schema = Schema(implementation = Map::class))]
        )
    ])
    @PostMapping("/otp/verify", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun verifyOtp(
        @Parameter(description = "OTP verification request with phone and code", required = true)
        @Valid @RequestBody request: OtpVerifyRequest
    ): ResponseEntity<Any> {
        return try {
            val phoneNumber = PhoneNumber(request.phone)
            val result = authenticationUseCase.verifyOtpAndAuthenticate(phoneNumber, request.code)

            when (result) {
                is VerifyOtpResult.Success -> {
                    ResponseEntity.ok(TokenResponse(result.accessToken, result.refreshToken))
                }
                is VerifyOtpResult.InvalidOtp -> {
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(mapOf("message" to "Invalid or expired OTP"))
                }
                is VerifyOtpResult.Error -> {
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(mapOf("message" to result.message))
                }
            }
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid phone number format: {}", request.phone)
            ResponseEntity.badRequest()
                .body(mapOf("message" to "Invalid phone number format"))
        }
    }

    @Operation(
        summary = "Admin Login",
        description = "Authenticates admin users with email and password"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Admin authenticated successfully",
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = TokenResponse::class)
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Invalid admin credentials",
            content = [Content(schema = Schema(implementation = Map::class))]
        )
    ])
    @PostMapping("/login/admin", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun adminLogin(
        @Parameter(description = "Admin login credentials", required = true)
        @Valid @RequestBody request: AdminLoginRequest
    ): ResponseEntity<Any> {
        // HARDCODED credentials for now as per requirements
        val adminEmail = env.getProperty("app.auth.admin-email", "admin@puntog.com")
        val adminPass = env.getProperty("app.auth.admin-password", "admin123")

        if (request.email != adminEmail || request.pass != adminPass) {
            logger.warn("Admin login failed for email: {}", request.email)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("message" to "Invalid credentials"))
        }

        val adminId = "admin-system-01"
        val adminRoles = listOf("ADMIN", "USER")
        val accessToken = jwtService.generateAccessToken(adminId, adminRoles)
        val refreshToken = jwtService.generateRefreshToken(adminId)

        logger.info("Admin tokens generated for admin ID {}", adminId)
        return ResponseEntity.ok(TokenResponse(accessToken, refreshToken))
    }

    @Operation(
        summary = "Advertiser Login",
        description = "Authenticates advertiser users with email and password"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Advertiser authenticated successfully",
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = TokenResponse::class)
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Invalid advertiser credentials",
            content = [Content(schema = Schema(implementation = Map::class))]
        )
    ])
    @PostMapping("/login/advertiser", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun advertiserLogin(
        @Parameter(description = "Advertiser login credentials", required = true)
        @Valid @RequestBody request: AdminLoginRequest
    ): ResponseEntity<Any> {
        // In a real system, this would check the advertiser table
        val advertiserEmail = env.getProperty("app.auth.advertiser-email", "anunciante@tosty.com")
        val advertiserPass = env.getProperty("app.auth.advertiser-password", "tosty123")

        if (request.email != advertiserEmail || request.pass != advertiserPass) {
            logger.warn("Advertiser login failed for email: {}", request.email)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("message" to "Invalid credentials"))
        }

        val advertiserId = "adv-tosty-01" // This would come from the database
        val advertiserRoles = listOf("ADVERTISER")
        val accessToken = jwtService.generateAccessToken(advertiserId, advertiserRoles)
        val refreshToken = jwtService.generateRefreshToken(advertiserId)

        logger.info("Advertiser tokens generated for advertiser ID {}", advertiserId)
        return ResponseEntity.ok(TokenResponse(accessToken, refreshToken))
    }

    @Operation(
        summary = "Revoke Token",
        description = "Revokes a specific JWT token by adding it to the blacklist. Can be used to invalidate compromised tokens."
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Token revoked successfully"
        ),
        ApiResponse(
            responseCode = "400",
            description = "Invalid token format or missing token",
            content = [Content(schema = Schema(implementation = Map::class))]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or expired token",
            content = [Content(schema = Schema(implementation = Map::class))]
        )
    ])
    @PostMapping("/revoke", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun revokeToken(
        @Parameter(description = "Authorization header with Bearer token to revoke", required = true)
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Any> {
        return try {
            if (!authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                    .body(mapOf("message" to "Invalid authorization header format. Use 'Bearer <token>'"))
            }

            val token = authHeader.substring(7) // Remove "Bearer " prefix

            if (token.isBlank()) {
                return ResponseEntity.badRequest()
                    .body(mapOf("message" to "Token cannot be empty"))
            }

            // Validate token before revoking to ensure it's a valid JWT
            if (!jwtService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("message" to "Invalid or expired token"))
            }

            authService.revokeToken(token)

            logger.info("Token revoked successfully")
            ResponseEntity.ok(mapOf("message" to "Token revoked successfully"))
        } catch (e: IllegalArgumentException) {
            logger.warn("Token revocation failed - invalid token: {}", e.message)
            ResponseEntity.badRequest()
                .body(mapOf("message" to "Invalid token format"))
        } catch (e: Exception) {
            logger.error("Token revocation failed: {}", e.message)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Token revocation failed"))
        }
    }

    @Operation(
        summary = "Logout",
        description = "Revokes the provided JWT token by adding it to the blacklist and logs out the user"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Logged out successfully"
        ),
        ApiResponse(
            responseCode = "400",
            description = "Invalid token format",
            content = [Content(schema = Schema(implementation = Map::class))]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or expired token",
            content = [Content(schema = Schema(implementation = Map::class))]
        )
    ])
    @PostMapping("/logout", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun logout(
        @Parameter(description = "Authorization header with Bearer token", required = true)
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Any> {
        return try {
            if (!authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                    .body(mapOf("message" to "Invalid authorization header format. Use 'Bearer <token>'"))
            }

            val token = authHeader.substring(7) // Remove "Bearer " prefix

            if (token.isBlank()) {
                return ResponseEntity.badRequest()
                    .body(mapOf("message" to "Token cannot be empty"))
            }

            // Validate token before revoking
            if (!jwtService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("message" to "Invalid or expired token"))
            }

            authService.revokeToken(token)

            logger.info("User logged out successfully")
            ResponseEntity.ok(mapOf("message" to "Logged out successfully"))
        } catch (e: IllegalArgumentException) {
            logger.warn("Logout failed - invalid token: {}", e.message)
            ResponseEntity.badRequest()
                .body(mapOf("message" to "Invalid token format"))
        } catch (e: Exception) {
            logger.error("Logout failed: {}", e.message)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Logout failed"))
        }
    }
}