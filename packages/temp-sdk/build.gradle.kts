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
    // Dependencies required by the generated code (e.g., Spring, Jackson, etc.)
    // These are minimal for compilation of the temp SDK
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

tasks.jar {
    archiveFileName.set("temp-sdk.jar")
}
