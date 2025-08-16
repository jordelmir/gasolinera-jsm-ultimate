# Gasolinera JSM - Monorepo

Este repositorio contiene el proyecto completo "Gasolinera JSM", una plataforma para digitalizar y monetizar la experiencia de recarga de combustible.

## Visión

Transformar cada recarga de combustible en una experiencia digital gratificante para el usuario y en una nueva línea de ingresos para la estación.

## Arquitectura

El sistema está construido sobre una arquitectura de microservicios utilizando un monorepo `nx`.

-   **Backend:** Kotlin + Spring Boot 3 + Java 17
-   **Frontend (Web):** Next.js + TypeScript + Tailwind CSS
-   **Móvil:** React Native (Expo) + TypeScript
-   **Base de Datos:** PostgreSQL
-   **Cache/Locks:** Redis
-   **Mensajería:** RabbitMQ (configurable a Kafka)
-   **Infraestructura:** Docker, Kubernetes (Helm), Terraform
-   **CI/CD:** GitHub Actions

## Requisitos Previos

-   Docker & Docker Compose
-   Node.js 20+
-   Java (JDK) 17+
-   `make` para usar los scripts de ayuda

## Setup (1 Comando)

1.  **Clonar el repositorio:**
    ```bash
    git clone <repo-url> gasolinera-jsm
    cd gasolinera-jsm
    ```

2.  **Configurar variables de entorno:**
    Copia el archivo de ejemplo y ajústalo según sea necesario.
    ```bash
    cp .env.example .env
    ```
    *Nota: Los servicios individuales también tienen archivos `.env.example` que son cargados por Docker Compose.*

3.  **Iniciar el entorno de desarrollo:**
    Este comando construirá las imágenes de Docker, iniciará los contenedores (bases de datos, broker, servicios, apps) y aplicará las migraciones de la base de datos.
    ```bash
    make dev
    ```

## Servicios y URLs Locales

Una vez que `make dev` se complete, los siguientes servicios estarán disponibles:

-   **API Gateway:** [http://localhost:8080](http://localhost:8080)
-   **Admin Dashboard:** [http://localhost:3000](http://localhost:3000)
-   **Advertiser Portal:** [http://localhost:3001](http://localhost:3001)
-   **PostgreSQL:** `localhost:5432`
-   **Redis:** `localhost:6379`
-   **RabbitMQ Management:** [http://localhost:15672](http://localhost:15672)

## Iniciar la App Móvil

Para ejecutar la aplicación móvil en un emulador o dispositivo físico:

1.  **Navega al directorio de la app:**
    ```bash
    cd apps/mobile
    ```

2.  **Instala dependencias:**
    ```bash
    npm install
    ```

3.  **Inicia el servidor de Expo:**
    ```bash
    npm start
    ```
    O desde la raíz del proyecto:
    ```bash
    make mobile
    ```
    Escanea el código QR con la app Expo Go en tu dispositivo.

## Scripts Útiles

-   `make dev`: Inicia todo el entorno de desarrollo con Docker Compose.
-   `make stop`: Detiene todos los contenedores.
-   `make clean`: Detiene y elimina todos los contenedores, volúmenes y redes.
-   `make test`: Ejecuta tests unitarios e de integración en todos los servicios.
-   `make seed`: Ejecuta el script de seeding para poblar la base de datos con datos de prueba.
-   `make k8s-up`: Despliega la aplicación en un clúster de Kubernetes local (ej. kind, k3d).
-   `make k8s-down`: Elimina el despliegue de Kubernetes.

## FAQ

**¿Cómo genero nuevos códigos QR firmados?**
Usa el script de `ops`. Requiere que el entorno esté corriendo para acceder a los secretos.
```bash
npm run nx -- run ops:qr:generate --count 10
```

**¿Dónde están las credenciales de prueba?**
Revisa el script de seeding `ops/scripts/dev/seed.ts` para ver los usuarios y estaciones de prueba que se crean.

**¿Cómo puedo ver los logs de un servicio específico?**
```bash
docker compose logs -f <nombre-del-servicio>
# Ejemplo: docker compose logs -f redemption-service
```
