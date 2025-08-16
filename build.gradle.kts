plugins {
    id("org.openapi.generator") version "7.0.1" // Use a recent stable version
}

openApiGenerate {
    generatorName.set("kotlin-spring")
    outputDir.set("$buildDir/generated")
    apiPackage.set("com.gasolinerajsm.sdk.api")
    modelPackage.set("com.gasolinerajsm.sdk.model")
    configOptions.set(mapOf(
        "dateLibrary" to "java8",
        "useCoroutines" to "true",
        "useBeanValidation" to "true",
        "interfaceOnly" to "true", // Generate interfaces only, not concrete implementations
        "skipFormModel" to "true",
        "skipDefaultInterface" to "true",
        "useTags" to "true",
        "openApiNullable" to "false"
    ))
}

tasks.register("generateAuthClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    dependsOn("openApiGenerate")
    generatorName.set("kotlin-spring")
    inputSpec.set("$projectDir/services/auth-service/openapi.yaml")
    outputDir.set("$buildDir/generated/auth-client")
    apiPackage.set("com.gasolinerajsm.sdk.auth.api")
    modelPackage.set("com.gasolinerajsm.sdk.auth.model")
    configOptions.set(mapOf(
        "dateLibrary" to "java8",
        "useCoroutines" to "true",
        "useBeanValidation" to "true",
        "interfaceOnly" to "true",
        "skipFormModel" to "true",
        "skipDefaultInterface" to "true",
        "useTags" to "true",
        "openApiNullable" to "false"
    ))
}

tasks.register("generateRedemptionClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    dependsOn("openApiGenerate")
    generatorName.set("kotlin-spring")
    inputSpec.set("$projectDir/services/redemption-service/openapi.yaml")
    outputDir.set("$buildDir/generated/redemption-client")
    apiPackage.set("com.gasolinerajsm.sdk.redemption.api")
    modelPackage.set("com.gasolinerajsm.sdk.redemption.model")
    configOptions.set(mapOf(
        "dateLibrary" to "java8",
        "useCoroutines" to "true",
        "useBeanValidation" to "true",
        "interfaceOnly" to "true",
        "skipFormModel" to "true",
        "skipDefaultInterface" to "true",
        "useTags" to "true",
        "openApiNullable" to "false"
    ))
}

tasks.register("generateAdEngineClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    dependsOn("openApiGenerate")
    generatorName.set("kotlin-spring")
    inputSpec.set("$projectDir/services/ad-engine/openapi.yaml")
    outputDir.set("$buildDir/generated/ad-engine-client")
    apiPackage.set("com.gasolinerajsm.sdk.adengine.api")
    modelPackage.set("com.gasolinerajsm.sdk.adengine.model")
    configOptions.set(mapOf(
        "dateLibrary" to "java8",
        "useCoroutines" to "true",
        "useBeanValidation" to "true",
        "interfaceOnly" to "true",
        "skipFormModel" to "true",
        "skipDefaultInterface" to "true",
        "useTags" to "true",
        "openApiNullable" to "false"
    ))
}

tasks.register("generateRaffleClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    dependsOn("openApiGenerate")
    generatorName.set("kotlin-spring")
    inputSpec.set("$projectDir/services/raffle-service/openapi.yaml")
    outputDir.set("$buildDir/generated/raffle-client")
    apiPackage.set("com.gasolinerajsm.sdk.raffle.api")
    modelPackage.set("com.gasolinerajsm.sdk.raffle.model")
    configOptions.set(mapOf(
        "dateLibrary" to "java8",
        "useCoroutines" to "true",
        "useBeanValidation" to "true",
        "interfaceOnly" to "true",
        "skipFormModel" to "true",
        "skipDefaultInterface" to "true",
        "useTags" to "true",
        "openApiNullable" to "false"
    ))
}

tasks.register("generateStationClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    dependsOn("openApiGenerate")
    generatorName.set("kotlin-spring")
    inputSpec.set("$projectDir/services/station-service/openapi.yaml")
    outputDir.set("$buildDir/generated/station-client")
    apiPackage.set("com.gasolinerajsm.sdk.station.api")
    modelPackage.set("com.gasolinerajsm.sdk.station.model")
    configOptions.set(mapOf(
        "dateLibrary" to "java8",
        "useCoroutines" to "true",
        "useBeanValidation" to "true",
        "interfaceOnly" to "true",
        "skipFormModel" to "true",
        "skipDefaultInterface" to "true",
        "useTags" to "true",
        "openApiNullable" to "false"
    ))
}
