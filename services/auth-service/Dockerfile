# Etapa 1: Compilación
FROM gradle:8.8.0-jdk17-alpine AS build
WORKDIR /home/gradle/project

# Copiar solo los archivos de build para cachear dependencias
COPY build.gradle.kts settings.gradle.kts ./
COPY services/auth-service/build.gradle.kts ./services/auth-service/

# Descargar dependencias
RUN gradle :services:auth-service:dependencies

# Copiar el resto del código fuente
COPY . .

# Construir la aplicación y renombrar el JAR ejecutable
RUN gradle :services:auth-service:build -x test -x detekt && \
    find /home/gradle/project/services/auth-service/build/libs -name "*.jar" ! -name "*-plain.jar" -exec mv {} /home/gradle/project/services/auth-service/build/libs/app.jar \

# Etapa 2: Producción
FROM openjdk:17-alpine

WORKDIR /app

# Copiar el JAR renombrado desde la etapa de compilación
COPY --from=build /home/gradle/project/services/auth-service/build/libs/app.jar .

# Exponer el puerto en el que corre la aplicación
EXPOSE 8081

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]