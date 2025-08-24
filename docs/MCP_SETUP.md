# Configuración MCP Máxima para Programación

Este proyecto está configurado con el entorno MCP (Model Context Protocol) más completo para desarrollo de software.

## 🚀 Instalación Rápida

```bash
# Ejecutar el script de configuración automática
./scripts/setup-mcp.sh
```

## 📋 Servidores MCP Configurados

### 🔧 Desarrollo Core (Habilitados)

| Servidor       | Descripción                | Herramientas Principales                               |
| -------------- | -------------------------- | ------------------------------------------------------ |
| **fetch**      | Peticiones HTTP/API        | `fetch`                                                |
| **filesystem** | Operaciones de archivos    | `read_file`, `write_file`, `list_directory`            |
| **postgres**   | Base de datos PostgreSQL   | `query`, `list_tables`, `describe_table`               |
| **docker**     | Gestión de contenedores    | `list_containers`, `container_logs`, `start_container` |
| **git**        | Control de versiones       | `git_status`, `git_commit`, `git_push`, `git_pull`     |
| **shell**      | Ejecución de comandos      | `execute_command`                                      |
| **memory**     | Almacenamiento persistente | `create_memory`, `search_memory`                       |
| **time**       | Operaciones de tiempo      | `get_current_time`, `format_time`                      |
| **sqlite**     | Base de datos SQLite       | `query`, `execute`                                     |
| **redis**      | Cache y almacenamiento     | `get`, `set`, `keys`                                   |

### ☁️ Cloud & DevOps (Habilitados)

| Servidor       | Descripción        | Herramientas Principales                        |
| -------------- | ------------------ | ----------------------------------------------- |
| **aws-docs**   | Documentación AWS  | `search_aws_docs`, `get_aws_doc`                |
| **kubernetes** | Gestión K8s        | `get_pods`, `get_services`, `describe_resource` |
| **puppeteer**  | Automatización web | `screenshot`, `navigate`, `click`               |

### 🔑 Servicios Externos (Requieren Configuración)

| Servidor         | Variable de Entorno            | Descripción            |
| ---------------- | ------------------------------ | ---------------------- |
| **github**       | `GITHUB_PERSONAL_ACCESS_TOKEN` | Integración con GitHub |
| **brave-search** | `BRAVE_API_KEY`                | Búsqueda web           |
| **slack**        | `SLACK_BOT_TOKEN`              | Integración con Slack  |
| **notion**       | `NOTION_API_KEY`               | Gestión de documentos  |
| **linear**       | `LINEAR_API_KEY`               | Gestión de issues      |
| **jira**         | `JIRA_API_TOKEN`               | Gestión de proyectos   |
| **sentry**       | `SENTRY_AUTH_TOKEN`            | Monitoreo de errores   |

## 🔧 Configuración Manual

### 1. Habilitar GitHub

```bash
# En .kiro/settings/mcp.json, cambiar "disabled": true a false
# Y configurar tu token:
export GITHUB_PERSONAL_ACCESS_TOKEN="ghp_your_token_here"
```

### 2. Habilitar Brave Search

```bash
# Obtener API key en https://api.search.brave.com/
export BRAVE_API_KEY="your_brave_api_key"
```

### 3. Habilitar Slack

```bash
# Crear bot en https://api.slack.com/apps
export SLACK_BOT_TOKEN="xoxb-your-token"
```

## 🎯 Casos de Uso

### Desarrollo Full-Stack

- **fetch**: Probar APIs y endpoints
- **postgres/sqlite**: Gestionar bases de datos
- **docker**: Manejar contenedores de desarrollo
- **git**: Control de versiones automático

### DevOps & Deployment

- **kubernetes**: Gestionar clusters
- **docker**: Orquestar contenedores
- **shell**: Automatizar deployments
- **aws-docs**: Consultar documentación

### Testing & QA

- **puppeteer**: Tests E2E automatizados
- **fetch**: Tests de API
- **memory**: Almacenar resultados de tests

### Productividad

- **memory**: Notas y documentación
- **time**: Gestión de tiempo
- **notion/obsidian**: Documentación avanzada

## 🔄 Reiniciar Servicios MCP

Si los servidores MCP no se conectan:

1. Reinicia Kiro completamente
2. Verifica que uvx esté en el PATH: `/Users/Jorge/.local/bin/uvx`
3. Ejecuta manualmente: `uvx mcp-server-fetch` para probar

## 📊 Monitoreo

Los logs de MCP se pueden ver en:

- Kiro → View → Output → MCP Logs
- Nivel de log configurado en `ERROR` para mejor rendimiento

## 🚀 Próximos Pasos

1. Configura las API keys para servicios externos
2. Personaliza las rutas en `filesystem` y `obsidian`
3. Ajusta las conexiones de base de datos según tu entorno
4. Explora las herramientas disponibles con cada servidor

¡Tu entorno MCP máximo está listo para programación avanzada! 🎉
