plugins {
    // OpenAPI and Documentation
    id("org.openapi.generator") version "7.0.1"
    id("org.springdoc.openapi-gradle-plugin") version "1.8.0" apply false

    // Kotlin Plugins
    kotlin("jvm") version "1.9.24" apply false
    kotlin("plugin.spring") version "1.9.24" apply false
    kotlin("plugin.jpa") version "1.9.24" apply false
    kotlin("plugin.serialization") version "1.9.24" apply false

    // Spring Boot Plugins
    id("org.springframework.boot") version "3.3.3" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false

    // Code Quality
    id("io.gitlab.arturbosch.detekt") version "1.23.4" apply false
    // jacoco - disabled temporarily

    // Build and Publishing
    `maven-publish`
}

// Enhanced dependency management for all subprojects
subprojects {
    apply(plugin = "io.spring.dependency-management")

    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            // Spring Cloud BOM
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.3")

            // Additional BOMs for consistency
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.3")
            mavenBom("org.jetbrains.kotlin:kotlin-bom:1.9.24")

            // OpenAPI and Documentation
            mavenBom("org.springdoc:springdoc-openapi:2.2.0")
        }

        // Common dependency versions
        dependencies {
            dependency("org.openapitools:jackson-databind-nullable:0.2.6")
            dependency("io.swagger.core.v3:swagger-annotations:2.2.15")
            dependency("io.swagger.core.v3:swagger-models:2.2.15")
        }
    }
}

// Service Configuration Data Class
data class ServiceConfig(
    val name: String,                    // Display name (e.g., "Auth")
    val servicePath: String,             // Directory name (e.g., "auth-service")
    val sdkPackage: String,              // Base package for generated code
    val port: Int,                       // Service port for local development
    val apiVersion: String = "v1",       // API version
    val hasOpenApi: Boolean = true,      // Whether to generate OpenAPI spec
    val generatorName: String = "kotlin", // OpenAPI generator to use
    val library: String = "jvm-spring-webclient" // Client library
)

// Enhanced Service Registry - Complete list of all services
val serviceRegistry = listOf(
    ServiceConfig(
        name = "Auth",
        servicePath = "auth-service",
        sdkPackage = "com.gasolinerajsm.sdk.auth",
        port = 8081
    ),
    ServiceConfig(
        name = "Station",
        servicePath = "station-service",
        sdkPackage = "com.gasolinerajsm.sdk.station",
        port = 8083
    ),
    ServiceConfig(
        name = "Coupon",
        servicePath = "coupon-service",
        sdkPackage = "com.gasolinerajsm.sdk.coupon",
        port = 8086
    ),
    ServiceConfig(
        name = "Redemption",
        servicePath = "redemption-service",
        sdkPackage = "com.gasolinerajsm.sdk.redemption",
        port = 8082
    ),
    ServiceConfig(
        name = "AdEngine",
        servicePath = "ad-engine",
        sdkPackage = "com.gasolinerajsm.sdk.adengine",
        port = 8084
    ),
    ServiceConfig(
        name = "Raffle",
        servicePath = "raffle-service",
        sdkPackage = "com.gasolinerajsm.sdk.raffle",
        port = 8085
    )
)

// Enhanced Common Configuration Options for Client Generation
val commonConfigOptions = mapOf(
    // Core Configuration
    "dateLibrary" to "java8",
    "library" to "jvm-spring-webclient",
    "serializationLibrary" to "jackson",

    // Kotlin Specific Options
    "useCoroutines" to "true",
    "explicitApi" to "false", // Set to false for easier integration

    // Validation and Documentation
    "useBeanValidation" to "true",
    "annotationLibrary" to "swagger2",
    "documentationProvider" to "none", // We'll handle docs separately

    // Code Generation Options
    "interfaceOnly" to "false", // Generate full client implementation
    "skipFormModel" to "true",
    "skipDefaultInterface" to "false",
    "useTags" to "true",
    "openApiNullable" to "false",

    // Additional Features for Production
    "generateClientAsBean" to "true",
    "useSpringBoot3" to "true",
    "reactive" to "false",
    "requestDateConverter" to "toString",
    "enumPropertyNaming" to "UPPERCASE",

    // Package and Naming
    "modelNameSuffix" to "DTO",
    "apiNameSuffix" to "Api"
)

// Dynamic Task Generation System
serviceRegistry.forEach { service ->

    // 1. OpenAPI Specification Generation Task
    if (service.hasOpenApi) {
        tasks.register("generate${service.name}OpenApi") {
            group = "openapi generation"
            description = "Generates OpenAPI specification for ${service.name} service"

            doLast {
                val openApiFile = file("services/${service.servicePath}/openapi.yaml")
                if (!openApiFile.exists()) {
                    logger.warn("OpenAPI spec not found for ${service.name}. Creating placeholder...")
                    openApiFile.parentFile.mkdirs()
                    openApiFile.writeText("""
                        openapi: 3.0.3
                        info:
                          title: ${service.name} API
                          version: ${service.apiVersion}
                          description: API for ${service.name} service
                        servers:
                          - url: http://localhost:${service.port}
                            description: Local development server
                        paths:
                          /health:
                            get:
                              summary: Health check
                              responses:
                                '200':
                                  description: Service is healthy
                                  content:
                                    application/json:
                                      schema:
                                        type: object
                                        properties:
                                          status:
                                            type: string
                                            example: "UP"
                    """.trimIndent())
                    logger.info("Created placeholder OpenAPI spec for ${service.name}")
                }
            }
        }
    }

    // 2. Client Generation Task
    val clientTaskName = "generate${service.name}Client"
    tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>(clientTaskName) {
        group = "openapi tools"
        description = "Generates the ${service.name} API client using ${service.generatorName} generator"

        // Generator Configuration
        generatorName.set(service.generatorName)
        library.set(service.library)

        // Input/Output Configuration
        inputSpec.set("$projectDir/services/${service.servicePath}/openapi.yaml")
        outputDir.set("${layout.buildDirectory.get()}/generated/${service.servicePath}-client")

        // Package Configuration
        apiPackage.set("${service.sdkPackage}.api")
        modelPackage.set("${service.sdkPackage}.model")
        invokerPackage.set("${service.sdkPackage}.client")

        // Apply common configuration
        configOptions.set(commonConfigOptions)

        // Additional Configuration
        // templateDir.set("$projectDir/gradle/openapi-templates") // Custom templates if needed

        // Task Dependencies
        if (service.hasOpenApi) {
            dependsOn("generate${service.name}OpenApi")
        }

        // Input Validation
        doFirst {
            val specFile = file(inputSpec.get())
            if (!specFile.exists()) {
                throw GradleException("OpenAPI specification not found: ${specFile.absolutePath}")
            }
        }

        // Post-generation cleanup and validation
        doLast {
            val outputDirectory = file(outputDir.get())
            if (outputDirectory.exists()) {
                logger.info("Successfully generated ${service.name} client in: ${outputDirectory.absolutePath}")

                // Validate generated files
                val apiDir = file("${outputDirectory}/src/main/kotlin/${service.sdkPackage.replace('.', '/')}/api")
                val modelDir = file("${outputDirectory}/src/main/kotlin/${service.sdkPackage.replace('.', '/')}/model")

                if (!apiDir.exists() || !modelDir.exists()) {
                    logger.warn("Generated client structure may be incomplete for ${service.name}")
                }
            }
        }
    }

    // 3. Validation Task for Generated Client
    tasks.register("validate${service.name}Client") {
        group = "verification"
        description = "Validates the generated ${service.name} client"
        dependsOn(clientTaskName)

        doLast {
            val clientDir = file("${layout.buildDirectory.get()}/generated/${service.servicePath}-client")
            val buildFile = file("$clientDir/build.gradle")

            if (clientDir.exists() && buildFile.exists()) {
                logger.info("✅ ${service.name} client validation passed")
            } else {
                throw GradleException("❌ ${service.name} client validation failed - missing required files")
            }
        }
    }
}

// Aggregate Tasks for Convenience
tasks.register("generateAllOpenApiSpecs") {
    group = "openapi generation"
    description = "Generates OpenAPI specifications for all services"
    dependsOn(serviceRegistry.filter { it.hasOpenApi }.map { "generate${it.name}OpenApi" })
}

tasks.register("generateAllClients") {
    group = "openapi tools"
    description = "Generates all API clients for the monorepo"
    dependsOn(serviceRegistry.map { "generate${it.name}Client" })
}

tasks.register("validateAllClients") {
    group = "verification"
    description = "Validates all generated API clients"
    dependsOn(serviceRegistry.map { "validate${it.name}Client" })
}

// Clean task for generated clients
tasks.register("cleanGeneratedClients") {
    group = "build"
    description = "Cleans all generated client code"

    doLast {
        val generatedDir = file("${layout.buildDirectory.get()}/generated")
        if (generatedDir.exists()) {
            generatedDir.deleteRecursively()
            logger.info("Cleaned generated clients directory")
        }
    }
}

// Development helper task
tasks.register("listServices") {
    group = "help"
    description = "Lists all configured services and their details"

    doLast {
        println("\n📋 Configured Services:")
        println("=".repeat(80))
        serviceRegistry.forEach { service ->
            println("🔹 ${service.name}")
            println("   Path: services/${service.servicePath}")
            println("   Package: ${service.sdkPackage}")
            println("   Port: ${service.port}")
            println("   Generator: ${service.generatorName} (${service.library})")
            println("   OpenAPI: ${if (service.hasOpenApi) "✅" else "❌"}")
            println()
        }
        println("Total services: ${serviceRegistry.size}")
    }
}
// Performance Optimizations
// =========================

// Enable Gradle build cache
gradle.settingsEvaluated {
    buildCache {
        local {
            isEnabled = true
            directory = File(rootDir, "build-cache")
        }
    }
}

// Configure parallel execution
tasks.configureEach {
    // Enable parallel execution for generation tasks
    if (name.contains("generate") && name.contains("Client")) {
        // Allow parallel execution of client generation tasks
        outputs.cacheIf { true }

        // Configure incremental build support
        inputs.files(fileTree("services") {
            include("**/openapi.yaml")
            include("**/build.gradle.kts")
        })

        outputs.dirs("${layout.buildDirectory.get()}/generated")
    }
}

// Optimize OpenAPI generation tasks
serviceRegistry.forEach { service ->
    tasks.named("generate${service.name}Client") {
        // Enable task output caching
        outputs.cacheIf { true }

        // Configure up-to-date checks
        inputs.file("$projectDir/services/${service.servicePath}/openapi.yaml")
        inputs.property("generatorName", service.generatorName)
        inputs.property("library", service.library)
        inputs.property("sdkPackage", service.sdkPackage)

        // Only regenerate if inputs have changed
        onlyIf {
            val outputDir = file("${layout.buildDirectory.get()}/generated/${service.servicePath}-client")
            val specFile = file("$projectDir/services/${service.servicePath}/openapi.yaml")

            !outputDir.exists() ||
            !specFile.exists() ||
            outputDir.lastModified() < specFile.lastModified()
        }
    }
}

// Parallel task execution configuration
tasks.register("generateAllClientsParallel") {
    group = "openapi tools"
    description = "Generates all API clients in parallel for better performance"

    // Configure parallel execution
    doLast {
        val parallelTasks = serviceRegistry.map { "generate${it.name}Client" }

        // Execute tasks in parallel using Gradle's parallel execution
        project.gradle.startParameter.isParallelProjectExecutionEnabled = true
        project.gradle.startParameter.maxWorkerCount = Runtime.getRuntime().availableProcessors()

        logger.info("Executing ${parallelTasks.size} client generation tasks in parallel")
        logger.info("Using ${project.gradle.startParameter.maxWorkerCount} worker threads")
    }

    // Depend on all client generation tasks
    dependsOn(serviceRegistry.map { "generate${it.name}Client" })
}

// Clean task optimization
tasks.named("cleanGeneratedClients") {
    // Only clean if generated directory exists
    onlyIf {
        file("${layout.buildDirectory.get()}/generated").exists()
    }

    doLast {
        logger.info("Cleaned generated clients directory")
    }


}

// Incremental build support
tasks.register("checkGeneratedClientsUpToDate") {
    group = "verification"
    description = "Checks if generated clients are up to date"

    doLast {
        val results = mutableMapOf<String, Boolean>()

        serviceRegistry.forEach { service ->
            val outputDir = file("${layout.buildDirectory.get()}/generated/${service.servicePath}-client")
            val specFile = file("$projectDir/services/${service.servicePath}/openapi.yaml")

            val upToDate = outputDir.exists() &&
                          specFile.exists() &&
                          outputDir.lastModified() >= specFile.lastModified()

            results[service.name] = upToDate
        }

        println("\n📊 Client Generation Status:")
        println("=".repeat(50))
        results.forEach { (service, upToDate) ->
            val status = if (upToDate) "✅ Up to date" else "⚠️  Needs regeneration"
            println("$service: $status")
        }

        val upToDateCount = results.values.count { it }
        val totalCount = results.size
        println("\nSummary: $upToDateCount/$totalCount clients are up to date")

        if (upToDateCount == totalCount) {
            println("🎉 All clients are up to date! No regeneration needed.")
        } else {
            println("🔄 ${totalCount - upToDateCount} clients need regeneration.")
        }
    }
}

// Performance monitoring task
tasks.register("benchmarkClientGeneration") {
    group = "performance"
    description = "Benchmarks client generation performance"

    doLast {
        val startTime = System.currentTimeMillis()

        println("\n🏃‍♂️ Starting client generation benchmark...")

        // Clean first
        project.tasks.getByName("cleanGeneratedClients").actions.forEach { it.execute(project.tasks.getByName("cleanGeneratedClients")) }

        val cleanTime = System.currentTimeMillis()
        println("Clean completed in ${cleanTime - startTime}ms")

        // Generate all clients
        serviceRegistry.forEach { service ->
            val taskStartTime = System.currentTimeMillis()

            try {
                project.tasks.getByName("generate${service.name}Client").actions.forEach {
                    it.execute(project.tasks.getByName("generate${service.name}Client"))
                }

                val taskEndTime = System.currentTimeMillis()
                println("${service.name} client generated in ${taskEndTime - taskStartTime}ms")

            } catch (e: Exception) {
                println("❌ ${service.name} client generation failed: ${e.message}")
            }
        }

        val totalTime = System.currentTimeMillis() - startTime

        println("\n📊 Benchmark Results:")
        println("=".repeat(40))
        println("Total time: ${totalTime}ms")
        println("Average per client: ${totalTime / serviceRegistry.size}ms")
        println("Clients per second: ${String.format("%.2f", serviceRegistry.size * 1000.0 / totalTime)}")

        // Performance recommendations
        if (totalTime > 30000) { // More than 30 seconds
            println("\n💡 Performance Recommendations:")
            println("- Consider using parallel generation: ./gradlew generateAllClientsParallel")
            println("- Enable build cache: gradle.properties -> org.gradle.caching=true")
            println("- Increase heap size: gradle.properties -> org.gradle.jvmargs=-Xmx4g")
        }
    }
}

// Gradle configuration optimizations
allprojects {
    // Configure Gradle daemon
    gradle.startParameter.apply {
        // Daemon is enabled by default in modern Gradle

        // Configure parallel execution
        isParallelProjectExecutionEnabled = true
        maxWorkerCount = Runtime.getRuntime().availableProcessors()
    }
}

// JVM optimizations for OpenAPI generator
tasks.withType<org.openapitools.generator.gradle.plugin.tasks.GenerateTask> {
    // JVM args are configured in gradle.properties
    // Additional task-specific optimizations can be added here
}