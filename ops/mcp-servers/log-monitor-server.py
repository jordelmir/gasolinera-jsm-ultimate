
from mcp.server.fastmcp import FastMCP
from mcp.server.fastmcp.utilities.logging import get_logger
import subprocess

logger = get_logger(__name__)
mcp = FastMCP("LogMonitor")

@mcp.tool()
async def get_docker_logs(container_name: str, tail: int = 100) -> str:
    """Retrieves logs from a specified Docker container."""
    command = ["docker", "logs", container_name, "--tail", str(tail)]
    try:
        result = subprocess.run(command, capture_output=True, text=True, check=True)
        return result.stdout
    except subprocess.CalledProcessError as e:
        logger.error(f"Failed to get logs for container {container_name}: {e.stderr}")
        raise Exception(f"Failed to get logs for container {container_name}: {e.stderr}")

if __name__ == "__main__":
    mcp.run()
