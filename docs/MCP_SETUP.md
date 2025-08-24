# MCP Server Configuration

This document explains how to configure MCP (Model Context Protocol) servers for the Gasolinera JSM project.

## Setup Instructions

1. Copy the example configuration:

   ```bash
   cp .kiro/settings/mcp.json.example .kiro/settings/mcp.json
   ```

2. Update the configuration with your credentials:
   - Replace `YOUR_GITHUB_TOKEN_HERE` with your GitHub Personal Access Token
   - Update database connection strings with your credentials
   - Adjust file paths as needed for your system

## Available MCP Servers

### Core Servers

- **fetch**: HTTP requests and web scraping
- **git**: Local Git operations
- **time**: Date and time operations
- **memory**: Persistent notes and memory
- **sqlite**: Local database operations

### Database Servers

- **postgres**: PostgreSQL database operations
- **github**: GitHub API operations (requires token)

## Security Notes

- Never commit the actual `mcp.json` file with real tokens
- The `mcp.json` file is gitignored to prevent accidental commits
- Use environment variables or secure vaults for production deployments

## Troubleshooting

If MCP servers fail to connect:

1. Verify `uvx` is installed: `pip install uv`
2. Check server package availability
3. Validate credentials and connection strings
4. Review MCP logs in Kiro IDE
