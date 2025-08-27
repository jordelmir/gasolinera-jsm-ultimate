# Implementation Plan

- [x] 1. Create phone number validation component
  - Create PhoneNumberValidator class with validation methods for E.164 and Costa Rican formats
  - Implement normalize() method to convert phone numbers to consistent E.164 format
  - Write comprehensive unit tests for all phone number validation scenarios
  - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 2. Create security audit logging component
  - Implement SecurityAuditLogger class with methods for logging security events
  - Create phone number hashing utility for secure logging without exposing sensitive data
  - Write unit tests for audit logging functionality
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 7.1, 7.2, 7.3, 7.4, 7.5_

- [x] 3. Update OtpProperties configuration class
  - Add new configuration properties for audit logging and sensitive data controls
  - Ensure backward compatibility with existing configuration
  - Write tests to verify configuration loading and default values
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 4. Update application.yml with new security configurations
  - Add phone validation settings and audit logging controls
  - Configure secure default values for all new properties
  - Document all configuration options with comments
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 5. Enhance AuthService with secure OTP generation
  - Replace kotlin.random.Random with java.security.SecureRandom in generateSecureOtp method
  - Update OTP generation to use cryptographically secure random numbers
  - Write unit tests to verify secure random number generation
  - _Requirements: 1.1, 1.2, 1.3_

- [x] 6. Implement enhanced phone number validation in AuthService
  - Integrate PhoneNumberValidator into sendOtp and verifyOtpAndIssueTokens methods
  - Add proper validation error handling and user-friendly error messages
  - Write integration tests for phone number validation in authentication flow
  - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 7. Enhance brute-force protection in AuthService
  - Implement single-use OTP verification to prevent OTP reuse
  - Add proper lockout mechanism with configurable duration
  - Enhance rate limiting logic with better attempt tracking
  - Write unit tests for all brute-force protection scenarios
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [x] 8. Remove sensitive information from logs in AuthService
  - Replace direct phone number logging with hashed identifiers
  - Remove OTP code from debug logs completely
  - Implement secure logging using SecurityAuditLogger for authentication events
  - Write tests to verify no sensitive data appears in logs
  - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 9. Add token revocation endpoint to AuthController
  - Create POST /auth/revoke endpoint that accepts JWT token for revocation
  - Implement proper authentication and authorization for revocation endpoint
  - Add comprehensive error handling and validation
  - Write integration tests for token revocation functionality
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [x] 10. Enhance JwtService token validation
  - Update validateToken method to check blacklist status before signature validation
  - Ensure proper error handling for blacklisted tokens
  - Add performance optimizations for blacklist checking
  - Write unit tests for enhanced token validation with blacklisting
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [x] 11. Create comprehensive integration tests
  - Write end-to-end tests for complete OTP authentication flow with security enhancements
  - Test brute-force protection scenarios with multiple failed attempts
  - Test token revocation and blacklisting in realistic scenarios
  - Verify phone number validation works correctly in full authentication flow
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1, 7.1_

- [x] 12. Add security monitoring and alerting capabilities
  - Implement security event aggregation for monitoring unusual patterns
  - Add metrics collection for authentication failures and lockouts
  - Create alerting logic for potential security incidents
  - Write tests for security monitoring functionality
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_
