
from mcp.server.fastmcp import FastMCP
from mcp.server.fastmcp.utilities.logging import get_logger
import subprocess

logger = get_logger(__name__)
mcp = FastMCP("BuildDeploy")

@mcp.tool()
async def build_app(app_name: str) -> str:
    """Builds a specified application using Nx."""
    command = ["npx", "nx", "build", app_name]
    try:
        result = subprocess.run(command, capture_output=True, text=True, check=True)
        return result.stdout
    except subprocess.CalledProcessError as e:
        logger.error(f"Build command failed for {app_name}: {e.stderr}")
        raise Exception(f"Build command failed for {app_name}: {e.stderr}")

@mcp.tool()
async def vercel_build() -> str:
    """Triggers the Vercel build process."""
    command = ["npx", "nx", "build", "owner-dashboard", "--prod"]
    try:
        result = subprocess.run(command, capture_output=True, text=True, check=True)
        return result.stdout
    except subprocess.CalledProcessError as e:
        logger.error(f"Vercel build command failed: {e.stderr}")
        raise Exception(f"Vercel build command failed: {e.stderr}")

@mcp.tool()
async def deploy_project() -> str:
    """Executes the project's deploy.sh script. Use with caution!"""
    command = ["bash", "scripts/deploy.sh"]
    try:
        result = subprocess.run(command, capture_output=True, text=True, check=True)
        return result.stdout
    except subprocess.CalledProcessError as e:
        logger.error(f"Deployment script failed: {e.stderr}")
        raise Exception(f"Deployment script failed: {e.stderr}")

if __name__ == "__main__":
    mcp.run()
