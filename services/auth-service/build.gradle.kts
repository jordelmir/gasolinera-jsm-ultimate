import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
    // id("io.gitlab.arturbosch.detekt") // Temporarily disabled due to version conflicts
    id("org.springdoc.openapi-gradle-plugin")
}

detekt {
    toolVersion = "1.23.4"
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
    // --- Spring Boot Starters ---
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // --- OpenAPI Documentation ---
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")
    implementation("org.springdoc:springdoc-openapi-starter-common")
    implementation("org.springframework.cloud:spring-cloud-starter-vault-config:4.1.3")

    // --- Observabilidad (Actuator + Prometheus) ---
    implementation("org.springframework.boot:spring-boot-starter-actuator") // NUEVO
    implementation("io.micrometer:micrometer-registry-prometheus")   // NUEVO

    // --- OpenTelemetry Tracing ---
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")

    // --- Logging ---
    implementation("net.logstash.logback:logstash-logback-encoder:7.4") // For structured JSON logging

    // --- Kotlin y Jackson ---
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // --- Base de Datos ---
    runtimeOnly("org.postgresql:postgresql")

    // --- JWT ---
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // --- Tests ---
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    // JUnit Platform dependencies
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("auth-service.jar")
}

// OpenAPI Configuration
openApi {
    apiDocsUrl.set("http://localhost:8081/v3/api-docs")
    outputDir.set(file("$projectDir"))
    outputFileName.set("openapi.yaml")
    waitTimeInSeconds.set(10)
}
