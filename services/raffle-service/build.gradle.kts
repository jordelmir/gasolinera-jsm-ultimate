plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    toolVersion = "1.23.7"
    buildUponDefaultConfig = true
    allRules = false
    baseline = file("detekt-baseline.xml")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
        txt.required.set(false)
        sarif.required.set(false)
    }
}

group = "com.gasolinerajsm"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7") // Detekt formatting rules
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-vault-config:4.1.3")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
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

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("raffle-service.jar")
}