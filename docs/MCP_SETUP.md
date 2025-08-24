# MCP (Model Context Protocol) Setup

Este documento explica cómo configurar los servidores MCP para el proyecto Gasolinera JSM.

## Servidores MCP Configurados

### 1. Fetch Server

- **Propósito**: Realizar requests HTTP a APIs externas
- **Paquete**: `mcp-server-fetch`
- **Uso**: Consultar APIs, webhooks, servicios externos

### 2. PostgreSQL Server

- **Propósito**: Conectar con la base de datos principal del proyecto
- **Paquete**: `mcp-server-postgres`
- **Configuración**: Requiere URL de conexión PostgreSQL
- **Uso**: Consultas, análisis de datos, reportes

### 3. Git Server

- **Propósito**: Operaciones Git locales
- **Paquete**: `mcp-server-git`
- **Uso**: Status, commits, branches, logs, diffs

### 4. Time Server

- **Propósito**: Operaciones con fechas y horarios
- **Paquete**: `mcp-server-time`
- **Uso**: Timestamps, conversiones de zona horaria, scheduling

### 5. SQLite Server

- **Propósito**: Base de datos temporal para análisis
- **Paquete**: `mcp-server-sqlite`
- **Uso**: Análisis de datos, reportes temporales, testing

### 6. Memory Server

- **Propósito**: Almacenamiento persistente de notas
- **Paquete**: `mcp-memory`
- **Uso**: Documentación, notas del proyecto, memoria entre sesiones

### 7. GitHub Server

- **Propósito**: Operaciones con repositorios GitHub
- **Endpoint**: `https://api.githubcopilot.com/mcp/`
- **Autenticación**: Bearer token de GitHub
- **Uso**: Repositorios, issues, pull requests, búsquedas

### 8. Docker Server

- **Propósito**: Gestión de contenedores Docker
- **Paquete**: `mcp-server-docker`
- **Uso**: Contenedores, imágenes, redes, volúmenes, logs
- **Requisitos**: Docker Desktop instalado y corriendo

### 9. Kubernetes Server

- **Propósito**: Gestión de clusters Kubernetes
- **Paquete**: `mcp-server-kubernetes`
- **Configuración**: Requiere `~/.kube/config`
- **Uso**: Pods, servicios, deployments, namespaces, logs

## Configuración

### Archivo de Configuración

- **Ubicación**: `.kiro/settings/mcp.json`
- **Ejemplo**: `.kiro/settings/mcp.json.example`

### Variables de Entorno Requeridas

- `GITHUB_PERSONAL_ACCESS_TOKEN`: Token de acceso personal de GitHub

### Instalación de Dependencias

Los servidores MCP se instalan automáticamente usando `uvx` cuando se configuran.

```bash
# Verificar que uvx esté instalado
uvx --version

# Si no está instalado, instalar uv primero
pip install uv
```

## Seguridad

⚠️ **IMPORTANTE**: Nunca subas tokens reales al repositorio.

- El archivo `.kiro/settings/mcp.json` está en `.gitignore`
- Usa `.kiro/settings/mcp.json.example` como plantilla
- Configura tus tokens localmente

## Uso en el Proyecto

Los servidores MCP permiten a Kiro:

1. **Análisis de Base de Datos**: Consultar PostgreSQL para métricas y reportes
2. **Gestión de Código**: Operaciones Git y GitHub automatizadas
3. **Integración de APIs**: Conectar con servicios externos
4. **Análisis Temporal**: Trabajar con fechas para raffles y cupones
5. **Documentación**: Mantener notas del proyecto
6. **Testing**: Usar SQLite para pruebas y análisis

## Troubleshooting

### Servidor No Conecta

1. Verificar que `uvx` esté instalado
2. Revisar logs en Kiro IDE
3. Verificar configuración de red/proxy

### Token de GitHub Inválido

1. Generar nuevo token en GitHub Settings
2. Actualizar `.kiro/settings/mcp.json` localmente
3. Verificar permisos del token

### Base de Datos PostgreSQL

1. Verificar que el servicio esté corriendo
2. Comprobar credenciales de conexión
3. Verificar acceso de red al puerto 5432
