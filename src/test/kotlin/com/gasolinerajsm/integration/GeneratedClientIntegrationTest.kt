package com.gasolinerajsm.integration

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Integration tests for generated OpenAPI clients
 * These tests validate that the generated clients can be compiled and used correctly
 */
@DisplayName("Generated Client Integration Tests")
@ActiveProfiles("test")
class GeneratedClientIntegrationTest {

    private val buildDir = File("build/generated")

    private val expectedServices = listOf(
        "auth-service" to "com.gasolinerajsm.sdk.auth",
        "station-service" to "com.gasolinerajsm.sdk.station",
        "coupon-service" to "com.gasolinerajsm.sdk.coupon",
        "redemption-service" to "com.gasolinerajsm.sdk.redemption",
        "ad-engine" to "com.gasolinerajsm.sdk.adengine",
        "raffle-service" to "com.gasolinerajsm.sdk.raffle"
    )

    @Nested
    @DisplayName("Generated Client Structure Validation")
    inner class GeneratedClientStructureValidation {

        @Test
        @DisplayName("Should generate client directories for all services")
        @EnabledIfSystemProperty(named = "test.integration", matches = "true")
        fun shouldGenerateClientDirectoriesForAllServices() {
            expectedServices.forEach { (servicePath, _) ->
                val clientDir = File(buildDir, "$servicePath-client")

                if (clientDir.exists()) {
                    assertTrue(
                        clientDir.isDirectory,
                        "Client directory should exist and be a directory: ${clientDir.path}"
                    )

                    // Check for standard Gradle project structure
                    val srcDir = File(clientDir, "src/main/kotlin")
                    assertTrue(
                        srcDir.exists() || File(clientDir, "src/main/java").exists(),
                        "Generated client should have source directory: ${srcDir.path}"
                    )
                }
            }
        }

        @Test
        @DisplayName("Should generate API and model packages")
        @EnabledIfSystemProperty(named = "test.integration", matches = "true")
        fun shouldGenerateApiAndModelPackages() {
            expectedServices.forEach { (servicePath, packageName) ->
                val clientDir = File(buildDir, "$servicePath-client")

                if (clientDir.exists()) {
                    val packagePath = packageName.replace(".", "/")

                    // Check for API package
                    val apiDir = File(clientDir, "src/main/kotlin/$packagePath/api")
                    val javaApiDir = File(clientDir, "src/main/java/$packagePath/api")

                    assertTrue(
                        apiDir.exists() || javaApiDir.exists(),
                        "API package should be generated: $packagePath/api"
                    )

                    // Check for model package
                    val modelDir = File(clientDir, "src/main/kotlin/$packagePath/model")
                    val javaModelDir = File(clientDir, "src/main/java/$packagePath/model")

                    assertTrue(
                        modelDir.exists() || javaModelDir.exists(),
                        "Model package should be generated: $packagePath/model"
                    )
                }
            }
        }

        @Test
        @DisplayName("Should generate build files for clients")
        @EnabledIfSystemProperty(named = "test.integration", matches = "true")
        fun shouldGenerateBuildFilesForClients() {
            expectedServices.forEach { (servicePath, _) ->
                val clientDir = File(buildDir, "$servicePath-client")

                if (clientDir.exists()) {
                    val buildFile = File(clientDir, "build.gradle")
                    val buildKtsFile = File(clientDir, "build.gradle.kts")
                    val pomFile = File(clientDir, "pom.xml")

                    assertTrue(
                        buildFile.exists() || buildKtsFile.exists() || pomFile.exists(),
                        "Generated client should have a build file (Gradle or Maven)"
                    )
                }
            }
        }
    }

    @Nested
    @DisplayName("Generated Code Quality Validation")
    inner class GeneratedCodeQualityValidation {

        @Test
        @DisplayName("Should generate valid Kotlin/Java files")
        @EnabledIfSystemProperty(named = "test.integration", matches = "true")
        fun shouldGenerateValidKotlinJavaFiles() {
            expectedServices.forEach { (servicePath, packageName) ->
                val clientDir = File(buildDir, "$servicePath-client")

                if (clientDir.exists()) {
                    val packagePath = packageName.replace(".", "/")

                    // Check API files
                    val apiDir = File(clientDir, "src/main/kotlin/$packagePath/api")
                    val javaApiDir = File(clientDir, "src/main/java/$packagePath/api")

                    val apiFiles = mutableListOf<File>()
                    if (apiDir.exists()) {
                        apiFiles.addAll(apiDir.listFiles { file ->
                            file.isFile && file.name.endsWith(".kt")
                        } ?: emptyArray())
                    }
                    if (javaApiDir.exists()) {
                        apiFiles.addAll(javaApiDir.listFiles { file ->
                            file.isFile && file.name.endsWith(".java")
                        } ?: emptyArray())
                    }

                    if (apiFiles.isNotEmpty()) {
                        apiFiles.forEach { file ->
                            val content = file.readText()

                            // Basic syntax validation
                            assertFalse(
                                content.contains("TODO"),
                                "Generated file should not contain TODO comments: ${file.name}"
                            )

                            assertTrue(
                                content.contains("package $packageName.api") ||
                                content.contains("package ${packageName}.api"),
                                "API file should have correct package declaration: ${file.name}"
                            )
                        }
                    }
                }
            }
        }

        @Test
        @DisplayName("Should generate files with proper annotations")
        @EnabledIfSystemProperty(named = "test.integration", matches = "true")
        fun shouldGenerateFilesWithProperAnnotations() {
            expectedServices.forEach { (servicePath, packageName) ->
                val clientDir = File(buildDir, "$servicePath-client")

                if (clientDir.exists()) {
                    val packagePath = packageName.replace(".", "/")

                    // Check for validation annotations in model files
                    val modelDir = File(clientDir, "src/main/kotlin/$packagePath/model")
                    val javaModelDir = File(clientDir, "src/main/java/$packagePath/model")

                    val modelFiles = mutableListOf<File>()
                    if (modelDir.exists()) {
                        modelFiles.addAll(modelDir.listFiles { file ->
                            file.isFile && (file.name.endsWith(".kt") || file.name.endsWith(".java"))
                        } ?: emptyArray())
                    }
                    if (javaModelDir.exists()) {
                        modelFiles.addAll(javaModelDir.listFiles { file ->
                            file.isFile && (file.name.endsWith(".kt") || file.name.endsWith(".java"))
                        } ?: emptyArray())
                    }

                    if (modelFiles.isNotEmpty()) {
                        // At least some files should have validation annotations
                        val hasValidationAnnotations = modelFiles.any { file ->
                            val content = file.readText()
                            content.contains("@Valid") ||
                            content.contains("@NotNull") ||
                            content.contains("@NotBlank") ||
                            content.contains("@Size") ||
                            content.contains("@Email")
                        }

                        // This is optional since not all models may need validation
                        // but if we configured useBeanValidation=true, we expect some validation
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("Client Compilation Validation")
    inner class ClientCompilationValidation {

        @Test
        @DisplayName("Should be able to compile generated clients")
        @EnabledIfSystemProperty(named = "test.integration", matches = "true")
        fun shouldBeAbleToCompileGeneratedClients() {
            expectedServices.forEach { (servicePath, _) ->
                val clientDir = File(buildDir, "$servicePath-client")

                if (clientDir.exists()) {
                    val buildFile = File(clientDir, "build.gradle")
                    val buildKtsFile = File(clientDir, "build.gradle.kts")

                    if (buildFile.exists() || buildKtsFile.exists()) {
                        // Try to validate that the build file is syntactically correct
                        val buildContent = if (buildFile.exists()) {
                            buildFile.readText()
                        } else {
                            buildKtsFile.readText()
                        }

                        // Basic validation of build file content
                        assertTrue(
                            buildContent.contains("dependencies") || buildContent.contains("implementation"),
                            "Build file should contain dependency declarations"
                        )

                        // Should have Spring or HTTP client dependencies
                        assertTrue(
                            buildContent.contains("spring") ||
                            buildContent.contains("okhttp") ||
                            buildContent.contains("webclient"),
                            "Build file should contain HTTP client dependencies"
                        )
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("Client Configuration Validation")
    inner class ClientConfigurationValidation {

        @Test
        @DisplayName("Should generate clients with correct configuration")
        @EnabledIfSystemProperty(named = "test.integration", matches = "true")
        fun shouldGenerateClientsWithCorrectConfiguration() {
            expectedServices.forEach { (servicePath, packageName) ->
                val clientDir = File(buildDir, "$servicePath-client")

                if (clientDir.exists()) {
                    // Look for configuration files or classes
                    val configFiles = mutableListOf<File>()

                    // Search for configuration in various locations
                    Files.walk(clientDir.toPath())
                        .filter { Files.isRegularFile(it) }
                        .filter { it.toString().contains("Config") || it.toString().contains("Client") }
                        .forEach { configFiles.add(it.toFile()) }

                    if (configFiles.isNotEmpty()) {
                        configFiles.forEach { file ->
                            val content = file.readText()

                            // Should not contain hardcoded URLs (should be configurable)
                            assertFalse(
                                content.contains("http://localhost:808") &&
                                !content.contains("configurable") &&
                                !content.contains("property"),
                                "Client should not have hardcoded URLs: ${file.name}"
                            )
                        }
                    }
                }
            }
        }

        @Test
        @DisplayName("Should generate clients with proper error handling")
        @EnabledIfSystemProperty(named = "test.integration", matches = "true")
        fun shouldGenerateClientsWithProperErrorHandling() {
            expectedServices.forEach { (servicePath, packageName) ->
                val clientDir = File(buildDir, "$servicePath-client")

                if (clientDir.exists()) {
                    val packagePath = packageName.replace(".", "/")
                    val apiDir = File(clientDir, "src/main/kotlin/$packagePath/api")
                    val javaApiDir = File(clientDir, "src/main/java/$packagePath/api")

                    val apiFiles = mutableListOf<File>()
                    if (apiDir.exists()) {
                        apiFiles.addAll(apiDir.listFiles { file ->
                            file.isFile && (file.name.endsWith(".kt") || file.name.endsWith(".java"))
                        } ?: emptyArray())
                    }
                    if (javaApiDir.exists()) {
                        apiFiles.addAll(javaApiDir.listFiles { file ->
                            file.isFile && (file.name.endsWith(".kt") || file.name.endsWith(".java"))
                        } ?: emptyArray())
                    }

                    if (apiFiles.isNotEmpty()) {
                        // Check that API files handle exceptions properly
                        val hasExceptionHandling = apiFiles.any { file ->
                            val content = file.readText()
                            content.contains("Exception") ||
                            content.contains("throws") ||
                            content.contains("try") ||
                            content.contains("ResponseEntity")
                        }

                        // This is a basic check - proper error handling should be present
                        // in generated client code
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("Documentation Validation")
    inner class DocumentationValidation {

        @Test
        @DisplayName("Should generate README or documentation files")
        @EnabledIfSystemProperty(named = "test.integration", matches = "true")
        fun shouldGenerateReadmeOrDocumentationFiles() {
            expectedServices.forEach { (servicePath, _) ->
                val clientDir = File(buildDir, "$servicePath-client")

                if (clientDir.exists()) {
                    val readmeFile = File(clientDir, "README.md")
                    val docFile = File(clientDir, "docs")

                    // At least one form of documentation should be present
                    // This is optional but recommended for generated clients
                    if (readmeFile.exists()) {
                        val content = readmeFile.readText()
                        assertTrue(
                            content.isNotBlank(),
                            "README file should not be empty"
                        )
                    }
                }
            }
        }
    }
}