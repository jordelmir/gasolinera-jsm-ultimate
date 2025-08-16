rootProject.name = "gasolinera-jsm-ultimate"

include(
    "services:ad-engine",
    "services:api-gateway",
    "services:auth-service",
    "services:raffle-service",
    "services:redemption-service",
    "services:station-service",
    "packages:internal-sdk", // NEW
    "packages:temp-sdk" // NEW: Temporary SDK for refactoring
)
