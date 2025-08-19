# detekt

## Metrics

* 36 number of properties

* 18 number of functions

* 13 number of classes

* 6 number of packages

* 12 number of kt files

## Complexity Report

* 380 lines of code (loc)

* 305 source lines of code (sloc)

* 203 logical lines of code (lloc)

* 15 comment lines of code (cloc)

* 29 cyclomatic complexity (mcc)

* 9 cognitive complexity

* 38 number of total code smells

* 4% comment source ratio

* 142 mcc per 1,000 lloc

* 187 code smells per 1,000 lloc

## Findings (38)

### exceptions, TooGenericExceptionCaught (1)

The caught exception is too generic. Prefer catching specific exceptions to the case that is currently handled.

[Documentation](https://detekt.dev/docs/rules/exceptions#toogenericexceptioncaught)

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/JwtService.kt:50:18
```
The caught exception is too generic. Prefer catching specific exceptions to the case that is currently handled.
```
```kotlin
47             Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
48             logger.debug("Token validated successfully.")
49             true
50         } catch (e: Exception) {
!!                  ^ error
51             logger.warn("Token validation failed: {}", e.message)
52             false
53         }

```

### formatting, FinalNewline (7)

Detects missing final newlines

[Documentation](https://detekt.dev/docs/rules/formatting#finalnewline)

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/controller/AuthController.kt:1:1
```
File must end with a newline (\n)
```
```kotlin
1 package com.gasolinerajsm.authservice.controller
! ^ error
2 
3 import com.gasolinerajsm.authservice.dto.AdminLoginRequest
4 import com.gasolinerajsm.authservice.dto.OtpRequest

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/dto/AdminLoginRequest.kt:1:1
```
File must end with a newline (\n)
```
```kotlin
1 package com.gasolinerajsm.authservice.dto
! ^ error
2 
3 import jakarta.validation.constraints.Email
4 import jakarta.validation.constraints.NotBlank

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/dto/OtpRequest.kt:1:1
```
File must end with a newline (\n)
```
```kotlin
1 package com.gasolinerajsm.authservice.dto
! ^ error
2 
3 import jakarta.validation.constraints.NotBlank
4 import jakarta.validation.constraints.Pattern

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/dto/OtpVerifyRequest.kt:1:1
```
File must end with a newline (\n)
```
```kotlin
1 package com.gasolinerajsm.authservice.dto
! ^ error
2 
3 import jakarta.validation.constraints.NotBlank
4 import jakarta.validation.constraints.Pattern

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/model/User.kt:1:1
```
File must end with a newline (\n)
```
```kotlin
1 package com.gasolinerajsm.authservice.model
! ^ error
2 
3 import jakarta.persistence.*
4 import java.time.Instant

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/AuthService.kt:1:1
```
File must end with a newline (\n)
```
```kotlin
1 package com.gasolinerajsm.authservice.service
! ^ error
2 
3 import com.gasolinerajsm.authservice.controller.TokenResponse
4 import org.slf4j.LoggerFactory

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/UserRepository.kt:1:1
```
File must end with a newline (\n)
```
```kotlin
1 package com.gasolinerajsm.authservice.service
! ^ error
2 
3 import com.gasolinerajsm.authservice.model.User
4 import org.springframework.data.jpa.repository.JpaRepository

```

### formatting, ImportOrdering (2)

Detects imports in non default order

[Documentation](https://detekt.dev/docs/rules/formatting#importordering)

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/controller/AuthController.kt:3:1
```
Imports must be ordered in lexicographic order without any empty lines in-between with "java", "javax", "kotlin" and aliases in the end
```
```kotlin
1 package com.gasolinerajsm.authservice.controller
2 
3 import com.gasolinerajsm.authservice.dto.AdminLoginRequest
! ^ error
4 import com.gasolinerajsm.authservice.dto.OtpRequest
5 import com.gasolinerajsm.authservice.dto.OtpVerifyRequest
6 import com.gasolinerajsm.authservice.dto.TokenResponse

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/JwtService.kt:3:1
```
Imports must be ordered in lexicographic order without any empty lines in-between with "java", "javax", "kotlin" and aliases in the end
```
```kotlin
1 package com.gasolinerajsm.authservice.service
2 
3 import io.jsonwebtoken.Jwts
! ^ error
4 import io.jsonwebtoken.SignatureAlgorithm
5 import io.jsonwebtoken.security.Keys
6 import org.springframework.beans.factory.annotation.Value

```

### formatting, NoWildcardImports (2)

Detects wildcard imports

[Documentation](https://detekt.dev/docs/rules/formatting#nowildcardimports)

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/controller/AuthController.kt:13:1
```
Wildcard import
```
```kotlin
10 import org.springframework.data.redis.core.StringRedisTemplate
11 import org.springframework.http.HttpStatus
12 import org.springframework.http.ResponseEntity
13 import org.springframework.web.bind.annotation.*
!! ^ error
14 import java.util.concurrent.TimeUnit
15 import org.slf4j.LoggerFactory
16 import org.springframework.core.env.Environment

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/model/User.kt:3:1
```
Wildcard import
```
```kotlin
1 package com.gasolinerajsm.authservice.model
2 
3 import jakarta.persistence.*
! ^ error
4 import java.time.Instant
5 import java.util.UUID
6 

```

### style, MagicNumber (17)

Report magic numbers. Magic number is a numeric literal that is not defined as a constant and hence it's unclear what the purpose of this number is. It's better to declare such numbers as constants and give them a proper name. By default, -1, 0, 1, and 2 are not considered to be magic numbers.

[Documentation](https://detekt.dev/docs/rules/style#magicnumber)

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/controller/AuthController.kt:31:24
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
28 
29     @PostMapping("/otp/request")
30     fun requestOtp(@Valid @RequestBody request: OtpRequest): ResponseEntity<Void> {
31         val otpCode = (100000..999999).random().toString() // Generate 6-digit code
!!                        ^ error
32         redisTemplate.opsForValue().set(request.phone, otpCode, 5, TimeUnit.MINUTES) // Store for 5 mins
33         logger.info("OTP requested for phone {}") // For manual testing
34         return ResponseEntity.ok().build()

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/controller/AuthController.kt:31:32
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
28 
29     @PostMapping("/otp/request")
30     fun requestOtp(@Valid @RequestBody request: OtpRequest): ResponseEntity<Void> {
31         val otpCode = (100000..999999).random().toString() // Generate 6-digit code
!!                                ^ error
32         redisTemplate.opsForValue().set(request.phone, otpCode, 5, TimeUnit.MINUTES) // Store for 5 mins
33         logger.info("OTP requested for phone {}") // For manual testing
34         return ResponseEntity.ok().build()

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/controller/AuthController.kt:32:65
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
29     @PostMapping("/otp/request")
30     fun requestOtp(@Valid @RequestBody request: OtpRequest): ResponseEntity<Void> {
31         val otpCode = (100000..999999).random().toString() // Generate 6-digit code
32         redisTemplate.opsForValue().set(request.phone, otpCode, 5, TimeUnit.MINUTES) // Store for 5 mins
!!                                                                 ^ error
33         logger.info("OTP requested for phone {}") // For manual testing
34         return ResponseEntity.ok().build()
35     }

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/AuthService.kt:21:34
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
18     private val logger = LoggerFactory.getLogger(AuthService::class.java)
19 
20     fun sendOtp(phone: String) {
21         val otp = Random.nextInt(100000, 999999).toString()
!!                                  ^ error
22         redisTemplate.opsForValue().set("otp:$phone", otp, 5, TimeUnit.MINUTES)
23         logger.info("Generated OTP for phone number ending in {}", phone.takeLast(4))
24 

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/AuthService.kt:21:42
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
18     private val logger = LoggerFactory.getLogger(AuthService::class.java)
19 
20     fun sendOtp(phone: String) {
21         val otp = Random.nextInt(100000, 999999).toString()
!!                                          ^ error
22         redisTemplate.opsForValue().set("otp:$phone", otp, 5, TimeUnit.MINUTES)
23         logger.info("Generated OTP for phone number ending in {}", phone.takeLast(4))
24 

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/AuthService.kt:22:60
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
19 
20     fun sendOtp(phone: String) {
21         val otp = Random.nextInt(100000, 999999).toString()
22         redisTemplate.opsForValue().set("otp:$phone", otp, 5, TimeUnit.MINUTES)
!!                                                            ^ error
23         logger.info("Generated OTP for phone number ending in {}", phone.takeLast(4))
24 
25         // In a real app, use the OtpSender interface to send the SMS

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/AuthService.kt:23:83
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
20     fun sendOtp(phone: String) {
21         val otp = Random.nextInt(100000, 999999).toString()
22         redisTemplate.opsForValue().set("otp:$phone", otp, 5, TimeUnit.MINUTES)
23         logger.info("Generated OTP for phone number ending in {}", phone.takeLast(4))
!!                                                                                   ^ error
24 
25         // In a real app, use the OtpSender interface to send the SMS
26         // otpSender.send(phone, "Your Gasolinera JSM code is: $otp")

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/AuthService.kt:33:93
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
30     fun verifyOtpAndIssueTokens(phone: String, code: String): TokenResponse {
31         val storedOtp = redisTemplate.opsForValue().get("otp:$phone")
32         if (storedOtp == null || storedOtp != code) {
33             logger.warn("Invalid OTP attempt for phone number ending in {}", phone.takeLast(4))
!!                                                                                             ^ error
34             throw IllegalArgumentException("Invalid or expired OTP")
35         }
36         logger.info("Successfully verified OTP for phone number ending in {}", phone.takeLast(4))

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/AuthService.kt:36:95
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
33             logger.warn("Invalid OTP attempt for phone number ending in {}", phone.takeLast(4))
34             throw IllegalArgumentException("Invalid or expired OTP")
35         }
36         logger.info("Successfully verified OTP for phone number ending in {}", phone.takeLast(4))
!!                                                                                               ^ error
37 
38         // val user = userRepository.findByPhone(phone) ?: userRepository.save(User(phone = phone))
39         val userId = "user-placeholder-id" // Replace with actual user ID

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/JwtService.kt:17:44
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
14     @Value("\${jwt.secret}")
15     private val jwtSecret: String
16 ) {
17     private val accessExpirationMs: Long = 15 * 60 * 1000 // 15 minutes
!!                                            ^ error
18     private val refreshExpirationMs: Long = 7 * 24 * 60 * 60 * 1000 // 7 days
19 
20     private val key: SecretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/JwtService.kt:17:49
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
14     @Value("\${jwt.secret}")
15     private val jwtSecret: String
16 ) {
17     private val accessExpirationMs: Long = 15 * 60 * 1000 // 15 minutes
!!                                                 ^ error
18     private val refreshExpirationMs: Long = 7 * 24 * 60 * 60 * 1000 // 7 days
19 
20     private val key: SecretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/JwtService.kt:17:54
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
14     @Value("\${jwt.secret}")
15     private val jwtSecret: String
16 ) {
17     private val accessExpirationMs: Long = 15 * 60 * 1000 // 15 minutes
!!                                                      ^ error
18     private val refreshExpirationMs: Long = 7 * 24 * 60 * 60 * 1000 // 7 days
19 
20     private val key: SecretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/JwtService.kt:18:45
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
15     private val jwtSecret: String
16 ) {
17     private val accessExpirationMs: Long = 15 * 60 * 1000 // 15 minutes
18     private val refreshExpirationMs: Long = 7 * 24 * 60 * 60 * 1000 // 7 days
!!                                             ^ error
19 
20     private val key: SecretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
21     private val logger = LoggerFactory.getLogger(JwtService::class.java)

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/JwtService.kt:18:49
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
15     private val jwtSecret: String
16 ) {
17     private val accessExpirationMs: Long = 15 * 60 * 1000 // 15 minutes
18     private val refreshExpirationMs: Long = 7 * 24 * 60 * 60 * 1000 // 7 days
!!                                                 ^ error
19 
20     private val key: SecretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
21     private val logger = LoggerFactory.getLogger(JwtService::class.java)

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/JwtService.kt:18:54
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
15     private val jwtSecret: String
16 ) {
17     private val accessExpirationMs: Long = 15 * 60 * 1000 // 15 minutes
18     private val refreshExpirationMs: Long = 7 * 24 * 60 * 60 * 1000 // 7 days
!!                                                      ^ error
19 
20     private val key: SecretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
21     private val logger = LoggerFactory.getLogger(JwtService::class.java)

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/JwtService.kt:18:59
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
15     private val jwtSecret: String
16 ) {
17     private val accessExpirationMs: Long = 15 * 60 * 1000 // 15 minutes
18     private val refreshExpirationMs: Long = 7 * 24 * 60 * 60 * 1000 // 7 days
!!                                                           ^ error
19 
20     private val key: SecretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
21     private val logger = LoggerFactory.getLogger(JwtService::class.java)

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/JwtService.kt:18:64
```
This expression contains a magic number. Consider defining it to a well named constant.
```
```kotlin
15     private val jwtSecret: String
16 ) {
17     private val accessExpirationMs: Long = 15 * 60 * 1000 // 15 minutes
18     private val refreshExpirationMs: Long = 7 * 24 * 60 * 60 * 1000 // 7 days
!!                                                                ^ error
19 
20     private val key: SecretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
21     private val logger = LoggerFactory.getLogger(JwtService::class.java)

```

### style, NewLineAtEndOfFile (7)

Checks whether files end with a line separator.

[Documentation](https://detekt.dev/docs/rules/style#newlineatendoffile)

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/controller/AuthController.kt:96:2
```
The file /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/controller/AuthController.kt is not ending with a new line.
```
```kotlin
93          logger.info("Advertiser tokens generated for advertiser ID {}", advertiserId)
94          return ResponseEntity.ok(TokenResponse(accessToken, refreshToken))
95      }
96  }
!!   ^ error

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/dto/AdminLoginRequest.kt:15:2
```
The file /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/dto/AdminLoginRequest.kt is not ending with a new line.
```
```kotlin
12     @field:NotBlank(message = "Password cannot be blank")
13     @field:Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
14     val pass: String
15 )
!!  ^ error

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/dto/OtpRequest.kt:10:2
```
The file /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/dto/OtpRequest.kt is not ending with a new line.
```
```kotlin
7      @field:NotBlank(message = "Phone number cannot be blank")
8      @field:Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Invalid phone number format")
9      val phone: String
10 )
!!  ^ error

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/dto/OtpVerifyRequest.kt:15:2
```
The file /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/dto/OtpVerifyRequest.kt is not ending with a new line.
```
```kotlin
12     @field:NotBlank(message = "OTP code cannot be blank")
13     @field:Size(min = 6, max = 6, message = "OTP code must be 6 digits")
14     val code: String
15 )
!!  ^ error

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/model/User.kt:27:2
```
The file /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/model/User.kt is not ending with a new line.
```
```kotlin
24     fun preUpdate() {
25         updatedAt = Instant.now()
26     }
27 }
!!  ^ error

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/AuthService.kt:50:2
```
The file /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/AuthService.kt is not ending with a new line.
```
```kotlin
47 
48         return TokenResponse(accessToken, refreshToken)
49     }
50 }
!!  ^ error

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/UserRepository.kt:10:2
```
The file /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/service/UserRepository.kt is not ending with a new line.
```
```kotlin
7  @Repository
8  interface UserRepository : JpaRepository<User, Long> {
9      fun findByPhone(phone: String): User?
10 }
!!  ^ error

```

### style, WildcardImport (2)

Wildcard imports should be replaced with imports using fully qualified class names. Wildcard imports can lead to naming conflicts. A library update can introduce naming clashes with your classes which results in compilation errors.

[Documentation](https://detekt.dev/docs/rules/style#wildcardimport)

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/controller/AuthController.kt:13:1
```
org.springframework.web.bind.annotation.* is a wildcard import. Replace it with fully qualified imports.
```
```kotlin
10 import org.springframework.data.redis.core.StringRedisTemplate
11 import org.springframework.http.HttpStatus
12 import org.springframework.http.ResponseEntity
13 import org.springframework.web.bind.annotation.*
!! ^ error
14 import java.util.concurrent.TimeUnit
15 import org.slf4j.LoggerFactory
16 import org.springframework.core.env.Environment

```

* /data/data/com.termux/files/home/gasolinera-jsm-ultimate/services/auth-service/src/main/kotlin/com/gasolinerajsm/authservice/model/User.kt:3:1
```
jakarta.persistence.* is a wildcard import. Replace it with fully qualified imports.
```
```kotlin
1 package com.gasolinerajsm.authservice.model
2 
3 import jakarta.persistence.*
! ^ error
4 import java.time.Instant
5 import java.util.UUID
6 

```

generated with [detekt version 1.23.6](https://detekt.dev/) on 2025-08-19 19:18:12 UTC
