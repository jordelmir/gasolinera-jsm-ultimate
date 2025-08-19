# Gasolinera JSM Ultimate Rewards

Este repositorio contiene la nueva aplicación web "Gasolinera JSM Ultimate Rewards", construida con React y Firebase, siguiendo la "Orden Maestra para Gemini CLI" proporcionada. Esta aplicación está diseñada para digitalizar y gamificar la experiencia de recarga de combustible, ofreciendo un sistema de cupones digitales y sorteos para clientes, así como dashboards específicos para pisteros y dueños de gasolineras.

## Arquitectura y Tecnologías

La aplicación se basa en la siguiente pila tecnológica:

-   **Workspace:** Nx
-   **Frontend:** Next.js (React)
-   **Backend/Base de Datos/Autenticación:** Google Firebase (Firestore y Authentication)
-   **Estilos:** Tailwind CSS

## Estructura del Proyecto

El proyecto es un monorepo gestionado por Nx. La aplicación web principal se encuentra en `apps/admin`:

-   `apps/admin/`
    -   `src/`
        -   `app/`: Vistas y layout principal de la aplicación.
        -   `components/`: Componentes reutilizables de UI.
        -   `lib/`: Utilidades y lógica de cliente.
-   `src/` (Legacy)
    -   `pages/`: Vistas principales de la aplicación (Login, Dashboards).
    -   `firebase/`: Configuración y servicios de Firebase.
    -   `context/`: Contexto de React para la gestión de la autenticación global.
    -   `App.jsx`: Componente principal que maneja el enrutamiento.

## Configuración y Ejecución Local

Sigue estos pasos para configurar y ejecutar la aplicación en tu entorno local:

### 1. Clonar el Repositorio

```bash
git clone [URL_DEL_REPOSITORIO]
cd gasolinera-jsm-ultimate
```

### 2. Configurar Firebase

1.  **Crear un Proyecto Firebase:** Si aún no tienes uno, crea un nuevo proyecto en la [Consola de Firebase](https://console.firebase.google.com/).
2.  **Configurar Firestore:** Habilita Cloud Firestore en tu proyecto de Firebase. Asegúrate de configurar las reglas de seguridad adecuadas para tus colecciones (`users`, `coupons`, `stations`).
3.  **Configurar Authentication:** Habilita el método de autenticación por "Email/Password" en tu proyecto de Firebase.
4.  **Obtener Credenciales de Configuración:** En la Consola de Firebase, ve a "Project settings" (Configuración del proyecto) y busca la sección "Your apps" (Tus aplicaciones). Selecciona la aplicación web y copia el objeto `firebaseConfig`.
5.  **Actualizar `src/firebase/config.js`:** Abre el archivo `src/firebase/config.js` en tu proyecto y reemplaza los placeholders con tus credenciales de Firebase:

    ```javascript
    const firebaseConfig = {
      apiKey: "TU_API_KEY",
      authDomain: "TU_AUTH_DOMAIN",
      projectId: "TU_PROJECT_ID",
      storageBucket: "TU_STORAGE_BUCKET",
      messagingSenderId: "TU_MESSAGING_SENDER_ID",
      appId: "TU_APP_ID"
    };
    ```

### 3. Instalar Dependencias

Navega al directorio raíz del proyecto e instala las dependencias:

```bash
npm install
```

### 4. Ejecutar la Aplicación

Para iniciar el servidor de desarrollo para la aplicación `admin`:

```bash
npx nx serve admin
```

La aplicación estará disponible en `http://localhost:4200` (o el puerto que Nx asigne).

## Roles de Usuario y Funcionalidades

La aplicación soporta tres roles de usuario principales:

### Cliente

-   **Dashboard Personal:** Visualiza el total de tickets acumulados para los sorteos.
-   **Escáner QR:** Permite escanear códigos QR generados por los pisteros para activar cupones y obtener tickets.
-   **Activación y Anuncios:** Opción de ver un anuncio corto para duplicar los tickets de una compra específica.
-   **Mis Tickets:** Lista de tokens únicos acumulados.
-   **Notificación de Ganador:** Pantalla de celebración en caso de ganar un sorteo.

### Pistero (Empleado)

-   **Interfaz Ultra-Simple:** Pantalla con un contador para ajustar la cantidad de "múltiplos de 5000" de la compra del cliente.
-   **Generación de QR:** Botón para generar un código QR único y de un solo uso para que el cliente lo escanee. La pantalla se resetea automáticamente para el siguiente cliente.

### Dueño (Administrador)

-   **Dashboard de Negocio:** Paneles de control visuales con métricas clave.
-   **Gestión de Sucursales:** Funcionalidad para agregar, ver y administrar gasolineras.
-   **Gestión de Empleados:** Registro de pisteros y asignación a sucursales.
-   **Analíticas de Rendimiento:** Gráficos y datos sobre tickets generados por empleado y sucursal.

## Lógica del Sorteo

La aplicación está diseñada para implementar una lógica de sorteo automatizada y transparente. Los sorteos (semanales y anuales) se realizarán de forma aleatoria entre todos los tickets activos, seleccionando un token ganador y notificando al usuario correspondiente.

## Consideraciones Adicionales

-   **Seguridad:** La autenticación se maneja a través de Firebase Authentication. Las reglas de seguridad de Firestore deben configurarse cuidadosamente para proteger los datos.
-   **Escalabilidad:** Firebase proporciona una solución escalable para la base de datos y la autenticación, adecuada para un crecimiento futuro.
-   **Despliegue:** La aplicación puede ser desplegada en plataformas como Vercel o Netlify después de ejecutar `npx nx build admin --prod`.

Esperamos que esta nueva aplicación sea una base sólida para "Gasolinera JSM Ultimate Rewards" y te ayude a alcanzar tus objetivos de digitalización y monetización. ¡No dudes en contactarme si tienes alguna pregunta o necesitas más asistencia!