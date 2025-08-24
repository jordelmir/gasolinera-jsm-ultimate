# API Gateway Client Generation Evaluation

## Decision: EXCLUDE api-gateway from client generation

### Rationale

After analyzing the api-gateway service structure, the decision is to **exclude** it from the OpenAPI client generation process for the following reasons:

### 1. **Architectural Purpose**

- The API Gateway serves as a routing and entry point, not a business service
- It forwards requests to actual business services (auth, station, coupon, etc.)
- Clients should interact with specific services directly through their generated SDKs

### 2. **Limited API Surface**

- Only contains fallback endpoints (`/fallback/*`)
- No business logic or domain-specific operations
- Fallback responses are for error handling, not normal client operations

### 3. **Client Usage Patterns**

- Applications typically don't need to call fallback endpoints directly
- Business operations are handled by specific service clients
- Gateway routing is transparent to client applications

### 4. **Best Practices**

- Microservice clients should use service-specific SDKs
- API Gateway is infrastructure, not a business service
- Reduces complexity and coupling in client applications

### Alternative Approach

Instead of generating a client for the API Gateway:

1. **Direct Service Communication**: Use generated clients for each business service
2. **Service Discovery**: Configure clients to communicate directly with services
3. **Load Balancing**: Handle at infrastructure level, not client level

### Services Included in Client Generation

The following services **are included** in client generation:

- ✅ auth-service (Authentication operations)
- ✅ station-service (Gas station management)
- ✅ coupon-service (QR coupon operations)
- ✅ redemption-service (Points and rewards)
- ✅ ad-engine (Advertisement serving)
- ✅ raffle-service (Lottery operations)
- ❌ api-gateway (Excluded - routing only)

This approach ensures clean separation of concerns and follows microservice best practices.
