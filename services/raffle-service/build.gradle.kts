plugins {
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"
    id("io.gitlab.arturbosch.detekt") version "1.23.6" // Detekt plugin
}

detekt {
    toolVersion = "1.23.6"
    buildUponDefaultConfig = true
    allRules = false // Set to true to enable all rules, or false to use default config
    // config = files("${project.rootDir}/detekt-config.yml") // Optional: path to custom Detekt config
    baseline = file("detekt-baseline.xml") // Optional: path to Detekt baseline file
    reports {
        xml { enabled = true }
        html { enabled = true }
        txt { enabled = false }
        sarif { enabled = false }
    }
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6") // Detekt formatting rules
}

group = "com.gasolinerajsm"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-vault-config:4.1.3")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.micrometer:micrometer-tracing-reporter-brave")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    // Database
    runtimeOnly("org.postgresql:postgresql")

    // HTTP Client for external API (e.g., Bitcoin block hash)
    implementation("org.springframework.boot:spring-boot-starter-webflux") // For WebClient

    // Merkle Tree Library (example, might need to implement custom)
    // implementation("com.github.merkletree:merkletree:1.0.0") // Placeholder, check for actual library

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

// tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
//     archiveFileName.set("raffle-service.jar")
//     mainClassName = "com.gasolinerajsm.raffleservice.RaffleServiceApplicationKt"
//     enabled = false // Temporarily disable bootJar to debug ClassNotFoundException
// }