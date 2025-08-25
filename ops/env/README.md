# Environment Configuration

Este directorio contiene las plantillas de configuración para diferentes entornos de la aplicación Gasolinera JSM Ultimate.

## Archivos de Configuración

### `.env.dev` - Desarrollo Local

- Configuración para desarrollo local
- Usa valores hardcodeados seguros para desarrollo
- Base de datos local, servicios en localhost
- Secretos de desarrollo (NO usar en producción)

### `.env.staging` - Entorno de Staging

- Configuración para pruebas pre-producción
- Usa variables de entorno para valores sensibles
- Simula configuración de producción
- Ideal para testing de integración

### `.env.prod` - Producción

- Configuración para entorno de producción
- **TODOS** los valores sensibles deben venir de variables de entorno
- Configuración optimizada para performance
- Seguridad máxima

## Uso

### Desarrollo Local

```bash
cp ops/env/.env.dev .env
make dev
```

### Staging

```bash
# Configurar variables de entorno primero
export POSTGRES_HOST="staging-db.example.com"
export POSTGRES_USER="staging_user"
# ... otras variables

cp ops/env/.env.staging .env
make deploy-staging
```

### Producción

```bash
# OBLIGATORIO: Configurar TODAS las variables de entorno
export POSTGRES_HOST="prod-db.example.com"
export JWT_SECRET="$(openssl rand -base64 32)"
# ... todas las variables requeridas

cp ops/env/.env.prod .env
make deploy-production
```

## Variables Críticas de Seguridad

### Obligatorias en Producción

- `JWT_SECRET` - Clave para firmar tokens JWT
- `JWT_REFRESH_SECRET` - Clave para tokens de refresh
- `QR_SIGNATURE_SECRET` - Clave para firmar códigos QR
- `QR_PUBLIC_KEY` - Clave pública para validar QR
- `POSTGRES_PASSWORD` - Contraseña de base de datos
- `RABBITMQ_PASS` - Contraseña de RabbitMQ
- `VAULT_TOKEN` - Token de acceso a Vault

### Generación de Secretos

```bash
# JWT Secret (32 bytes)
openssl rand -base64 32

# QR Keys (RSA 2048)
openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -pubout -out public.pem
```

## Validación de Configuración

Ejecutar el script de validación antes de deployment:

```bash
make ops:env:validate
```

Este script verificará que todas las variables requeridas estén configuradas.

## Integración con CI/CD

### GitHub Actions

Las variables se configuran en:

- Repository Secrets (producción)
- Environment Secrets (staging)
- Variables (no sensibles)

### Render.com

Las variables se configuran en:

- Service Environment Variables
- Environment Groups para reutilización

### Vercel

Las variables se configuran en:

- Project Settings > Environment Variables
- Por entorno: Development, Preview, Production

## Troubleshooting

### Error: Variable no definida

```bash
# Verificar que la variable esté exportada
echo $VARIABLE_NAME

# Verificar en el archivo .env
grep VARIABLE_NAME .env
```

### Error: Conexión a base de datos

```bash
# Verificar conectividad
pg_isready -h $POSTGRES_HOST -p $POSTGRES_PORT

# Verificar credenciales
psql -h $POSTGRES_HOST -U $POSTGRES_USER -d $POSTGRES_DB
```

### Error: Servicios no disponibles

```bash
# Verificar health checks
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
```
