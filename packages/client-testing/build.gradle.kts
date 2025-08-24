plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

// Disable bootJar for library packages
tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
    archiveClassifier = ""
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

group = "com.gasolinerajsm"
version = "1.0.0"

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-test")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

    // Testing frameworks
    implementation("io.mockk:mockk:1.13.8")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // JUnit Platform dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // TestContainers
    implementation("org.testcontainers:testcontainers:1.19.1")
    implementation("org.testcontainers:junit-jupiter:1.19.1")
    implementation("org.testcontainers:postgresql:1.19.1")

    // Jackson for JSON processing
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // WebTestClient
    implementation("org.springframework:spring-webflux")

    // Client configuration (optional dependency)
    compileOnly(project(":packages:client-config"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    // TestContainers configuration
    systemProperty("testcontainers.reuse.enable", "true")
    systemProperty("testcontainers.ryuk.disabled", "true")

    // JVM options for tests
    jvmArgs("-XX:+UnlockExperimentalVMOptions")

    // Test logging
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = false
    }
}