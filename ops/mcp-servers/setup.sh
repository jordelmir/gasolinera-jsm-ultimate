#!/bin/bash

# Setup script for Gasolinera JSM PostgreSQL MCP Server

echo "Setting up PostgreSQL MCP Server for Gasolinera JSM..."

# Create virtual environment if it doesn't exist
if [ ! -d "venv" ]; then
    echo "Creating virtual environment..."
    python3 -m venv venv
fi

# Activate virtual environment
source venv/bin/activate

# Install dependencies
echo "Installing dependencies..."
pip install -r ops/mcp-servers/requirements.txt

# Make the server executable
chmod +x ops/mcp-servers/postgres-server.py

echo "Setup complete!"
echo ""
echo "To use this server, add the following to your .kiro/settings/mcp.json:"
echo ""
echo '{
  "mcpServers": {
    "gasolinera-postgres": {
      "command": "python3",
      "args": ["'$(pwd)'/postgres-server.py"],
      "cwd": "'$(pwd)'",
      "env": {
        "DB_HOST": "localhost",
        "DB_PORT": "5432",
        "DB_NAME": "puntog",
        "DB_USER": "puntog",
        "DB_PASSWORD": "changeme",
        "PYTHONPATH": "'$(pwd)'/venv/lib/python3.*/site-packages"
      },
      "disabled": false,
      "autoApprove": [
        "query_database",
        "execute_command",
        "list_tables",
        "describe_table",
        "get_business_metrics"
      ]
    }
  }
}'