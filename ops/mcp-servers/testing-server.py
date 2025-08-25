
from mcp.server.fastmcp import FastMCP
from mcp.server.fastmcp.utilities.logging import get_logger
import subprocess

logger = get_logger(__name__)
mcp = FastMCP("Testing")

@mcp.tool()
async def run_all_tests() -> str:
    """Runs all Nx tests in the project."""
    command = ["npx", "nx", "run-many", "--target=test", "--all"]
    try:
        result = subprocess.run(command, capture_output=True, text=True, check=True)
        return result.stdout
    except subprocess.CalledProcessError as e:
        logger.error(f"Run all tests command failed: {e.stderr}")
        raise Exception(f"Run all tests command failed: {e.stderr}")

@mcp.tool()
async def run_affected_tests() -> str:
    """Runs Nx tests only for affected projects."""
    command = ["npx", "nx", "affected", "--target=test"]
    try:
        result = subprocess.run(command, capture_output=True, text=True, check=True)
        return result.stdout
    except subprocess.CalledProcessError as e:
        logger.error(f"Run affected tests command failed: {e.stderr}")
        raise Exception(f"Run affected tests command failed: {e.stderr}")

if __name__ == "__main__":
    mcp.run()
