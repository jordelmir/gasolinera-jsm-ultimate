rootProject.name = "gasolinera-jsm-ultimate"

include(
    "services:auth-service",
    "services:coupon-service",
    "services:station-service", // ✅ Fixed and enabled
    "services:api-gateway", // ✅ Fixed and enabled
    "services:ad-engine", // ✅ Fixed and enabled
    "services:raffle-service", // ✅ Fixed and enabled
    "services:redemption-service", // ✅ Re-enabled
    "packages:internal-sdk", // NEW
    "packages:client-config", // Client configuration utilities
    "packages:client-testing", // Client testing utilities
    // Temporarily disabled due to compilation issues:
    // "packages:temp-sdk"
)
