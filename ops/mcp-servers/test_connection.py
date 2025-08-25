#!/usr/bin/env python3
"""
Test script to verify PostgreSQL connection
"""

import asyncio
import asyncpg
import os

async def test_connection():
    try:
        # Database connection configuration
        DB_CONFIG = {
            "host": "localhost",
            "port": 5432,
            "database": "puntog",
            "user": "puntog",
            "password": "changeme"
        }

        print("Testing PostgreSQL connection...")
        print(f"Connecting to: {DB_CONFIG['user']}@{DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}")

        # Test connection
        conn = await asyncpg.connect(**DB_CONFIG)

        # Test query
        version = await conn.fetchval("SELECT version()")
        print(f"✅ Connection successful!")
        print(f"PostgreSQL version: {version}")

        # Test listing tables
        tables = await conn.fetch("""
            SELECT table_name, table_type
            FROM information_schema.tables
            WHERE table_schema = 'public'
            ORDER BY table_name;
        """)

        print(f"\n📋 Tables in database ({len(tables)} found):")
        for table in tables:
            print(f"  - {table['table_name']} ({table['table_type']})")

        await conn.close()
        print("\n🎉 All tests passed! MCP server should work correctly.")

    except Exception as e:
        print(f"❌ Connection failed: {e}")
        return False

    return True

if __name__ == "__main__":
    success = asyncio.run(test_connection())
    exit(0 if success else 1)