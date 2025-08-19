# Gasolinera JSM - Monorepo

Este repositorio contiene el proyecto completo "Gasolinera JSM", una plataforma para digitalizar y monetizar la experiencia de recarga de combustible.

## Propósito

<!-- Vercel deployment trigger -->

Transformar cada recarga de combustible en una experiencia digital gratificante para el usuario y en una nueva línea de ingresos para la estación.

## Arquitectura y Stack Tecnológico

El sistema está construido sobre una arquitectura de microservicios utilizando un monorepo `nx`.

-   **Backend:** Kotlin + Spring Boot 3 + Java 17
-   **Frontend (Web):** Next.js + TypeScript + Tailwind CSS
-   **Móvil:** React Native (Expo) + TypeScript
-   **Base de Datos:** PostgreSQL
-   **Cache/Locks:** Redis
-   **Mensajería:** RabbitMQ (con soporte para Kafka a través de Debezium)
-   **Observabilidad:** OpenTelemetry y Jaeger para tracing distribuido.
-   **Patrones de Diseño:** Patrón Outbox con Debezium para garantizar la entrega de mensajes.
-   **API y SDK:** OpenAPI para la definición de APIs y generación de un SDK interno para la comunicación entre servicios.
-   **Infraestructura:** Docker, Kubernetes (Helm), Terraform
-   **CI/CD:** GitHub Actions

## Cómo Compilar y Ejecutar

Sigue estos pasos para levantar el entorno de desarrollo en tu máquina local.

### Requisitos Previos

Asegúrate de tener las siguientes herramientas instaladas:

-   Docker Desktop
-   JDK 17 (Java Development Kit)
-   Git

### Pasos

1.  **Clonar el Repositorio:**
    ```bash
    git clone https://github.com/tu-usuario/gasolinera-jsm-ultimate.git
    cd gasolinera-jsm-ultimate
    ```

2.  **Configurar el Entorno:**
    Copia el archivo de ejemplo `.env.example` para crear tu configuración local.
    ```bash
    cp .env.example .env
    ```
    Abre el archivo `.env` y rellena las variables de entorno necesarias.

3.  **Construir las Imágenes Docker:**
    Este comando utiliza el `Makefile` para construir las imágenes de todos los servicios definidos en `docker-compose.yml`.
    ```bash
    make build-all
    ```

4.  **Levantar los Servicios:**
    Una vez construidas las imágenes, levanta todos los contenedores en modo detached.
    ```bash
    make dev
    ```

5.  **Verificar los Servicios:**
    Para ver los logs de todos los servicios y asegurarte de que están funcionando correctamente, usa:
    ```bash
    make logs
    ```
    También puedes ver los logs de un servicio específico:
    ```bash
    docker compose logs -f <nombre-del-servicio>
    ```

## Servicios y URLs Locales

Una vez que `make dev` se complete, los siguientes servicios estarán disponibles:

-   **API Gateway:** [http://localhost:8080](http://localhost:8080)
-   **Admin Dashboard:** [http://localhost:3000](http://localhost:3000)
-   **Advertiser Portal:** [http://localhost:3001](http://localhost:3001)
-   **PostgreSQL:** `localhost:5432`
-   **Redis:** `localhost:6379`
-   **RabbitMQ Management:** [http://localhost:15672](http://localhost:15672)
-   **Jaeger UI (Tracing):** [http://localhost:16686](http://localhost:16686)

## Scripts Útiles

-   `make build-all`: Construye las imágenes Docker de todos los servicios.
-   `make dev`: Inicia todo el entorno de desarrollo con Docker Compose.
-   `make stop`: Detiene todos los contenedores.
-   `make clean`: Detiene y elimina todos los contenedores, volúmenes y redes.
-   `make logs`: Muestra los logs de todos los servicios.
-   `make test`: Ejecuta tests unitarios e de integración en todos los servicios.
-   `make seed`: Ejecuta el script de seeding para poblar la base de datos con datos de prueba.
-   `make mobile`: Inicia el servidor de desarrollo de la app móvil (Expo).
-   `make k8s-up`: Despliega la aplicación en un clúster de Kubernetes local.
-   `make k8s-down`: Elimina el despliegue de Kubernetes.

This is a demo line for a Pull Request.

## FAQ

**¿Cómo genero nuevos códigos QR firmados?**
Usa el script de `ops`. Requiere que el entorno esté corriendo para acceder a los secretos.
```bash
npm run nx -- run ops:qr:generate --count 10
```

**¿Dónde están las credenciales de prueba?**
Revisa el script de seeding `ops/scripts/dev/seed.ts` para ver los usuarios y estaciones de prueba que se crean.
