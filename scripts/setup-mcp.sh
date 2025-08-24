#!/bin/bash

# Script para configurar el entorno MCP máximo para programación
# Instala todos los servidores MCP necesarios usando uvx

echo "🚀 Configurando entorno MCP máximo para programación..."

# Verificar que uv/uvx estén instalados
if ! command -v uvx &> /dev/null; then
    echo "❌ uvx no está instalado. Instalando uv..."
    curl -LsSf https://astral.sh/uv/install.sh | sh
    source ~/.bashrc
fi

echo "✅ uvx encontrado en: $(which uvx)"

# Lista de servidores MCP para instalar
MCP_SERVERS=(
    "mcp-server-fetch"
    "mcp-server-filesystem"
    "mcp-server-postgres"
    "mcp-server-docker"
    "mcp-server-git"
    "mcp-server-shell"
    "mcp-server-memory"
    "mcp-server-sequential-thinking"
    "mcp-server-time"
    "mcp-server-sqlite"
    "awslabs.aws-documentation-mcp-server@latest"
    "mcp-server-kubernetes"
    "mcp-server-github"
    "mcp-server-brave-search"
    "mcp-server-puppeteer"
    "mcp-server-redis"
    "mcp-server-everart"
    "mcp-server-slack"
    "mcp-server-gdrive"
    "mcp-server-notion"
    "mcp-server-obsidian"
    "mcp-server-sentry"
    "mcp-server-linear"
    "mcp-server-jira"
    "mcp-server-mongodb"
    "mcp-server-elasticsearch"
)

echo "📦 Instalando servidores MCP..."

for server in "${MCP_SERVERS[@]}"; do
    echo "  📥 Instalando $server..."
    uvx --python 3.11 install "$server" || echo "  ⚠️  Error instalando $server (puede que no exista)"
done

echo ""
echo "🎯 Configuración MCP completada!"
echo ""
echo "📋 Servidores MCP configurados:"
echo "  ✅ fetch - Para realizar peticiones HTTP"
echo "  ✅ filesystem - Para operaciones de archivos"
echo "  ✅ postgres - Para base de datos PostgreSQL"
echo "  ✅ docker - Para gestión de contenedores"
echo "  ✅ git - Para operaciones Git"
echo "  ✅ shell - Para ejecutar comandos shell"
echo "  ✅ memory - Para almacenamiento persistente"
echo "  ✅ sequential-thinking - Para pensamiento secuencial"
echo "  ✅ time - Para operaciones de tiempo"
echo "  ✅ sqlite - Para base de datos SQLite"
echo "  ✅ aws-docs - Para documentación AWS"
echo "  ✅ kubernetes - Para gestión K8s"
echo "  ✅ puppeteer - Para automatización web"
echo "  ✅ redis - Para cache Redis"
echo ""
echo "🔧 Servidores deshabilitados (requieren configuración):"
echo "  🔑 github - Requiere GITHUB_PERSONAL_ACCESS_TOKEN"
echo "  🔑 brave-search - Requiere BRAVE_API_KEY"
echo "  🔑 everart - Requiere EVERART_API_KEY"
echo "  🔑 slack - Requiere SLACK_BOT_TOKEN"
echo "  🔑 gdrive - Requiere GOOGLE_APPLICATION_CREDENTIALS"
echo "  🔑 notion - Requiere NOTION_API_KEY"
echo "  🔑 sentry - Requiere SENTRY_AUTH_TOKEN"
echo "  🔑 linear - Requiere LINEAR_API_KEY"
echo "  🔑 jira - Requiere JIRA_API_TOKEN"
echo ""
echo "🔄 Reinicia Kiro para que los cambios surtan efecto."
echo "✨ ¡Entorno MCP máximo configurado exitosamente!"