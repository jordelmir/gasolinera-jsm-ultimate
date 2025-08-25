
from mcp.server.fastmcp import FastMCP
from mcp.server.fastmcp.utilities.logging import get_logger
import subprocess

logger = get_logger(__name__)
mcp = FastMCP("CodeQuality")

@mcp.tool()
async def run_lint(path: str = ".") -> str:
    """Runs the Nx lint command for the specified path."""
    command = ["npx", "nx", "lint", path]
    try:
        result = subprocess.run(command, capture_output=True, text=True, check=True)
        return result.stdout
    except subprocess.CalledProcessError as e:
        logger.error(f"Lint command failed: {e.stderr}")
        raise Exception(f"Lint command failed: {e.stderr}")

@mcp.tool()
async def run_format(path: str = ".") -> str:
    """Runs the Nx format command for the specified path."""
    command = ["npx", "nx", "format:write", path]
    try:
        result = subprocess.run(command, capture_output=True, text=True, check=True)
        return result.stdout
    except subprocess.CalledProcessError as e:
        logger.error(f"Format command failed: {e.stderr}")
        raise Exception(f"Format command failed: {e.stderr}")

if __name__ == "__main__":
    mcp.run()
