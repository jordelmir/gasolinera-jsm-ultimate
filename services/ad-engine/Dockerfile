# Etapa 1: Compilación
FROM gradle:8.8.0-jdk17-alpine AS build
WORKDIR /home/gradle/project

# Copiar solo los archivos de build para cachear dependencias
COPY build.gradle.kts settings.gradle.kts ./
COPY services/ad-engine/build.gradle.kts ./services/ad-engine/

# Descargar dependencias
RUN gradle :services:ad-engine:dependencies

# Copiar el resto del código fuente
COPY . .

# Construir la aplicación
RUN gradle :services:ad-engine:build -x test

# Etapa 2: Producción
FROM openjdk:17-alpine

WORKDIR /app

# Copiar el JAR específico y ejecutable
COPY --from=build /home/gradle/project/services/ad-engine/build/libs/ad-engine.jar app.jar

# Exponer el puerto en el que corre la aplicación
EXPOSE 8084

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
