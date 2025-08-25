#!/usr/bin/env python3
"""
Custom PostgreSQL MCP Server for Gasolinera JSM
Provides database access tools for the gamification platform
"""

import asyncio
import json
import sys
from typing import Any, Dict, List, Optional
import asyncpg
import os
from mcp.server import Server
from mcp.types import (
    Resource,
    Tool,
    TextContent,
    ImageContent,
    EmbeddedResource,
    LoggingLevel
)
import mcp.server.stdio
import mcp.types as types

# Database connection configuration
DB_CONFIG = {
    "host": os.getenv("DB_HOST", "localhost"),
    "port": int(os.getenv("DB_PORT", "5432")),
    "database": os.getenv("DB_NAME", "puntog"),
    "user": os.getenv("DB_USER", "puntog"),
    "password": os.getenv("DB_PASSWORD", "changeme")
}

server = Server("gasolinera-postgres")

class DatabaseManager:
    def __init__(self):
        self.pool = None

    async def connect(self):
        """Initialize database connection pool"""
        try:
            self.pool = await asyncpg.create_pool(**DB_CONFIG)
            return True
        except Exception as e:
            print(f"Database connection failed: {e}", file=sys.stderr)
            return False

    async def execute_query(self, query: str, params: tuple = ()) -> List[Dict[str, Any]]:
        """Execute a SELECT query and return results"""
        if not self.pool:
            await self.connect()

        async with self.pool.acquire() as conn:
            try:
                rows = await conn.fetch(query, *params)
                return [dict(row) for row in rows]
            except Exception as e:
                raise Exception(f"Query execution failed: {e}")

    async def execute_command(self, query: str, params: tuple = ()) -> str:
        """Execute INSERT/UPDATE/DELETE commands"""
        if not self.pool:
            await self.connect()

        async with self.pool.acquire() as conn:
            try:
                result = await conn.execute(query, *params)
                return result
            except Exception as e:
                raise Exception(f"Command execution failed: {e}")

db_manager = DatabaseManager()

@server.list_tools()
async def handle_list_tools() -> List[Tool]:
    """List available database tools"""
    return [
        Tool(
            name="query_database",
            description="Execute SELECT queries on the Gasolinera JSM database",
            inputSchema={
                "type": "object",
                "properties": {
                    "query": {
                        "type": "string",
                        "description": "SQL SELECT query to execute"
                    },
                    "params": {
                        "type": "array",
                        "description": "Query parameters (optional)",
                        "items": {"type": "string"},
                        "default": []
                    }
                },
                "required": ["query"]
            }
        ),
        Tool(
            name="execute_command",
            description="Execute INSERT, UPDATE, DELETE commands on the database",
            inputSchema={
                "type": "object",
                "properties": {
                    "query": {
                        "type": "string",
                        "description": "SQL command to execute"
                    },
                    "params": {
                        "type": "array",
                        "description": "Query parameters (optional)",
                        "items": {"type": "string"},
                        "default": []
                    }
                },
                "required": ["query"]
            }
        ),
        Tool(
            name="list_tables",
            description="List all tables in the database",
            inputSchema={
                "type": "object",
                "properties": {}
            }
        ),
        Tool(
            name="describe_table",
            description="Get schema information for a specific table",
            inputSchema={
                "type": "object",
                "properties": {
                    "table_name": {
                        "type": "string",
                        "description": "Name of the table to describe"
                    }
                },
                "required": ["table_name"]
            }
        ),
        Tool(
            name="get_business_metrics",
            description="Get key business metrics for the gamification platform",
            inputSchema={
                "type": "object",
                "properties": {
                    "date_from": {
                        "type": "string",
                        "description": "Start date (YYYY-MM-DD format, optional)"
                    },
                    "date_to": {
                        "type": "string",
                        "description": "End date (YYYY-MM-DD format, optional)"
                    }
                }
            }
        )
    ]

@server.call_tool()
async def handle_call_tool(name: str, arguments: Dict[str, Any]) -> List[types.TextContent]:
    """Handle tool calls"""

    if name == "query_database":
        query = arguments.get("query", "")
        params = tuple(arguments.get("params", []))

        try:
            results = await db_manager.execute_query(query, params)
            return [types.TextContent(
                type="text",
                text=json.dumps(results, indent=2, default=str)
            )]
        except Exception as e:
            return [types.TextContent(
                type="text",
                text=f"Error executing query: {str(e)}"
            )]

    elif name == "execute_command":
        query = arguments.get("query", "")
        params = tuple(arguments.get("params", []))

        try:
            result = await db_manager.execute_command(query, params)
            return [types.TextContent(
                type="text",
                text=f"Command executed successfully: {result}"
            )]
        except Exception as e:
            return [types.TextContent(
                type="text",
                text=f"Error executing command: {str(e)}"
            )]

    elif name == "list_tables":
        query = """
        SELECT table_name, table_type
        FROM information_schema.tables
        WHERE table_schema = 'public'
        ORDER BY table_name;
        """

        try:
            results = await db_manager.execute_query(query)
            return [types.TextContent(
                type="text",
                text=json.dumps(results, indent=2)
            )]
        except Exception as e:
            return [types.TextContent(
                type="text",
                text=f"Error listing tables: {str(e)}"
            )]

    elif name == "describe_table":
        table_name = arguments.get("table_name", "")
        query = """
        SELECT
            column_name,
            data_type,
            is_nullable,
            column_default,
            character_maximum_length
        FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = $1
        ORDER BY ordinal_position;
        """

        try:
            results = await db_manager.execute_query(query, (table_name,))
            return [types.TextContent(
                type="text",
                text=json.dumps(results, indent=2)
            )]
        except Exception as e:
            return [types.TextContent(
                type="text",
                text=f"Error describing table: {str(e)}"
            )]

    elif name == "get_business_metrics":
        # Business metrics specific to the gamification platform
        date_from = arguments.get("date_from")
        date_to = arguments.get("date_to")

        # Base metrics query - adjust table names based on your actual schema
        metrics_queries = {
            "total_users": "SELECT COUNT(*) as count FROM users",
            "active_stations": "SELECT COUNT(*) as count FROM gas_stations WHERE active = true",
            "total_coupons_generated": "SELECT COUNT(*) as count FROM coupons",
            "total_redemptions": "SELECT COUNT(*) as count FROM redemptions",
            "weekly_raffle_participants": "SELECT COUNT(DISTINCT user_id) as count FROM raffle_entries WHERE raffle_type = 'weekly'",
            "annual_raffle_participants": "SELECT COUNT(DISTINCT user_id) as count FROM raffle_entries WHERE raffle_type = 'annual'"
        }

        try:
            metrics = {}
            for metric_name, query in metrics_queries.items():
                try:
                    result = await db_manager.execute_query(query)
                    metrics[metric_name] = result[0]['count'] if result else 0
                except:
                    metrics[metric_name] = "Table not found or query error"

            return [types.TextContent(
                type="text",
                text=json.dumps(metrics, indent=2)
            )]
        except Exception as e:
            return [types.TextContent(
                type="text",
                text=f"Error getting business metrics: {str(e)}"
            )]

    else:
        return [types.TextContent(
            type="text",
            text=f"Unknown tool: {name}"
        )]

async def main():
    """Main server entry point"""
    # Initialize database connection
    connected = await db_manager.connect()
    if not connected:
        print("Failed to connect to database", file=sys.stderr)
        sys.exit(1)

    # Run the server
    async with mcp.server.stdio.stdio_server() as (read_stream, write_stream):
        await server.run(
            read_stream,
            write_stream,
            server.create_initialization_options()
        )

if __name__ == "__main__":
    asyncio.run(main())