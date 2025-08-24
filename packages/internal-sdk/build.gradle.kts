plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm") version "1.9.24" // Use the same Kotlin version as other services
}

group = "com.gasolinerajsm"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Include generated clients as source
    implementation(project.files("${rootProject.buildDir}/generated/auth-client/src/main/kotlin"))
    implementation(project.files("${rootProject.buildDir}/generated/redemption-client/src/main/kotlin"))
    implementation(project.files("${rootProject.buildDir}/generated/ad-engine-client/src/main/kotlin"))
    implementation(project.files("${rootProject.buildDir}/generated/raffle-client/src/main/kotlin"))
    implementation(project.files("${rootProject.buildDir}/generated/station-client/src/main/kotlin"))

    // Dependencies required by the generated code (e.g., Spring, Jackson, etc.)
    implementation("org.springframework.boot:spring-boot-starter-web:3.3.3") // Use the same Spring Boot version as services
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2") // Use the same Jackson version
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.24") // Use the same Kotlin version
    implementation("jakarta.validation:jakarta.validation-api:3.0.2") // For @Valid annotations
    implementation("io.swagger.parser.v3:swagger-parser:2.1.10") // For OpenAPI annotations
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

tasks.jar {
    archiveFileName.set("internal-sdk.jar")
}
