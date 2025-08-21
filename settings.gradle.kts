rootProject.name = "gasolinera-jsm-ultimate"

include(
    "services:auth-service",
    "services:coupon-service",
    "services:station-service", // ✅ Fixed and enabled
    "services:api-gateway", // ✅ Fixed and enabled
    "services:ad-engine", // ✅ Fixed and enabled
    "packages:internal-sdk", // NEW
    // Temporarily disabled due to compilation issues:
    // "services:raffle-service",
    // "services:redemption-service",
    // "packages:temp-sdk"
)
