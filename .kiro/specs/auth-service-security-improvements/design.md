# Design Document

## Overview

This design addresses critical security vulnerabilities in the AuthService by implementing cryptographically secure OTP generation, brute-force protection, proper validation, token revocation mechanisms, and secure logging practices. The improvements maintain the existing Spring Boot architecture while enhancing security posture significantly.

The current AuthService already has a solid foundation with Redis integration, JWT token management, and basic OTP functionality. This design builds upon these strengths while addressing the identified security gaps.

## Architecture

### Current Architecture Strengths

- Clean separation of concerns with dedicated services (AuthService, JwtService, UserService)
- Redis integration for OTP storage and rate limiting
- JWT token generation and validation
- Configuration externalization with OtpProperties
- Proper dependency injection and logging

### Security Enhancement Architecture

The enhanced architecture maintains the existing structure while adding:

1. **Secure Random Number Generation**: Replace kotlin.random.Random with java.security.SecureRandom
2. **Enhanced Rate Limiting**: Implement comprehensive brute-force protection
3. **Phone Number Validation**: Add robust validation for international and local formats
4. **Token Blacklisting**: Extend existing JWT blacklisting capabilities
5. **Secure Logging**: Remove sensitive data from logs while maintaining audit trails
6. **Configuration Management**: Externalize all security-related configurations

## Components and Interfaces

### Enhanced AuthService

The AuthService will be updated with the following improvements:

```kotlin
@Service
class AuthService(
    private val redisTemplate: StringRedisTemplate,
    private val jwtService: JwtService,
    private val userService: UserService,
    private val otpProperties: OtpProperties,
    private val phoneValidator: PhoneNumberValidator
) {
    private val secureRandom = SecureRandom()
    // Enhanced implementation
}
```

**Key Methods:**

- `sendOtp(phone: String)` - Enhanced with secure generation and validation
- `verifyOtpAndIssueTokens(phone: String, code: String)` - Enhanced with rate limiting
- `revokeToken(token: String)` - Token revocation capability
- `generateSecureOtp()` - Cryptographically secure OTP generation

### New PhoneNumberValidator Component

A dedicated validator for phone number validation:

```kotlin
@Component
class PhoneNumberValidator {
    fun validate(phoneNumber: String): ValidationResult
    fun normalize(phoneNumber: String): String
    fun isValidFormat(phoneNumber: String): Boolean
}
```

**Validation Rules:**

- Support E.164 international format (+1234567890)
- Support Costa Rican local formats (8888-8888, 88888888)
- Normalize to consistent E.164 format
- Validate length and character constraints

### Enhanced JwtService

The existing JwtService already has blacklisting capabilities that will be leveraged:

**Existing Methods (Already Implemented):**

- `blacklistToken(token: String)` - Add token to blacklist
- `isTokenBlacklisted(token: String)` - Check blacklist status
- `validateToken(token: String)` - Validate and check blacklist

### Enhanced OtpProperties Configuration

Extended configuration properties:

```kotlin
@ConfigurationProperties(prefix = "app.otp")
data class OtpProperties(
    var expirationMinutes: Long = 5,
    var length: Int = 6,
    var maxAttempts: Int = 5,
    var lockoutMinutes: Long = 15,
    var minValue: Int = 100000,
    var maxValue: Int = 999999,
    var enableAuditLogging: Boolean = true,
    var logSensitiveData: Boolean = false // Should be false in production
)
```

### Security Audit Logger

A dedicated component for security event logging:

```kotlin
@Component
class SecurityAuditLogger {
    fun logOtpGenerated(phoneHash: String)
    fun logOtpVerificationFailed(phoneHash: String, attemptCount: Int)
    fun logAccountLocked(phoneHash: String)
    fun logTokenRevoked(tokenId: String)
    fun logSuccessfulAuthentication(userId: String)
}
```

## Data Models

### Rate Limiting Data Structure

Redis keys and data structures for rate limiting:

```
otp:{phone_number} -> OTP_CODE (TTL: expiration_minutes)
otp_attempts:{phone_number} -> ATTEMPT_COUNT (TTL: lockout_minutes)
otp_lockout:{phone_number} -> LOCKED_TIMESTAMP (TTL: lockout_minutes)
```

### Token Blacklist Structure (Already Implemented)

```
jwt_blacklist:{token} -> "revoked" (TTL: token_expiration)
```

### Audit Log Structure

Structured logging format for security events:

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "event_type": "OTP_VERIFICATION_FAILED",
  "phone_hash": "sha256_hash_of_phone",
  "attempt_count": 3,
  "ip_address": "192.168.1.100",
  "user_agent": "Mobile App v1.2.3"
}
```

## Error Handling

### Enhanced Error Responses

Standardized error responses that don't leak sensitive information:

1. **Invalid Phone Number**: "Invalid phone number format"
2. **Too Many Attempts**: "Too many failed attempts. Please try again in X minutes"
3. **Invalid OTP**: "Invalid or expired OTP"
4. **Account Locked**: "Account temporarily locked due to security reasons"
5. **Token Revoked**: "Token has been revoked"

### Error Logging Strategy

- Log security events with sufficient detail for investigation
- Never log actual OTP codes or full phone numbers
- Use hashed identifiers for correlation
- Include attempt counts and timing information
- Log IP addresses and user agents when available

## Testing Strategy

### Unit Tests

1. **Secure OTP Generation Tests**
   - Verify SecureRandom usage
   - Test OTP format and length
   - Ensure unpredictability

2. **Rate Limiting Tests**
   - Test attempt counting
   - Verify lockout behavior
   - Test successful reset

3. **Phone Validation Tests**
   - Test various phone number formats
   - Verify normalization
   - Test edge cases

4. **Token Blacklisting Tests**
   - Test token revocation
   - Verify blacklist checking
   - Test TTL behavior

### Integration Tests

1. **End-to-End OTP Flow**
   - Generate OTP → Verify OTP → Issue Tokens
   - Test with rate limiting scenarios
   - Test with invalid inputs

2. **Security Scenario Tests**
   - Brute force attack simulation
   - Token revocation scenarios
   - Phone number validation edge cases

### Security Tests

1. **Penetration Testing Scenarios**
   - OTP brute force attempts
   - Token replay attacks
   - Phone number enumeration attempts

2. **Performance Tests**
   - Rate limiting under load
   - Redis performance with blacklists
   - Concurrent authentication requests

## Configuration Management

### Application.yml Structure

```yaml
app:
  otp:
    expiration-minutes: 5
    length: 6
    max-attempts: 5
    lockout-minutes: 15
    min-value: 100000
    max-value: 999999
    enable-audit-logging: true
    log-sensitive-data: false
  jwt:
    secret: ${JWT_SECRET:default-secret-change-in-production}
  phone:
    validation:
      enable-international: true
      enable-local-costa-rica: true
      default-country-code: '+506'
```

### Environment-Specific Overrides

- **Development**: More lenient validation, debug logging enabled
- **Staging**: Production-like security, audit logging enabled
- **Production**: Maximum security, no sensitive data logging

## Security Considerations

### Cryptographic Security

- Use `java.security.SecureRandom` for all random number generation
- Ensure proper entropy sources are available
- Regular security audits of random number generation

### Rate Limiting Strategy

- Implement exponential backoff for repeated failures
- Consider IP-based rate limiting in addition to phone-based
- Monitor for distributed brute force attacks

### Data Protection

- Hash phone numbers in logs using SHA-256
- Never store or log actual OTP codes
- Implement proper data retention policies

### Token Security

- Leverage existing JWT blacklisting mechanism
- Consider shorter token lifetimes for high-security operations
- Implement token rotation strategies

### Monitoring and Alerting

- Alert on unusual authentication patterns
- Monitor rate limiting effectiveness
- Track token revocation rates
- Alert on configuration changes

## Implementation Phases

### Phase 1: Core Security Fixes

- Replace random number generation
- Implement phone number validation
- Remove sensitive data from logs

### Phase 2: Enhanced Rate Limiting

- Implement comprehensive brute force protection
- Add security audit logging
- Enhance error handling

### Phase 3: Advanced Security Features

- Implement token revocation endpoints
- Add security monitoring
- Performance optimization

This design maintains backward compatibility while significantly enhancing the security posture of the authentication system. The modular approach allows for incremental implementation and testing of each security improvement.
