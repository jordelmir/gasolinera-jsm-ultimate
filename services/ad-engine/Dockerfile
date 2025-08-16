# Build Stage
FROM gradle:8.8.0-jdk17 AS build
WORKDIR /app

# Copy Gradle wrapper and settings file from the root of the project
# Assuming the Docker build context is the project root
COPY gradlew .
COPY gradle/ gradle/
COPY settings.gradle.kts .

# Copy the specific service's files
COPY services/ad-engine/build.gradle.kts ./services/ad-engine/
COPY services/ad-engine/src ./services/ad-engine/src/

# Build the application JAR for the specific service
WORKDIR /app/services/ad-engine
RUN ../../gradlew bootJar

# Final Stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create a non-root user
RUN groupadd --system appuser && useradd --system --gid appuser appuser
USER appuser

# Copy the built JAR from the build stage
COPY --from=build /app/services/ad-engine/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]