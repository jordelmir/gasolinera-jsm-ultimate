
from mcp.server.fastmcp import FastMCP
from mcp.server.fastmcp.utilities.logging import get_logger
import subprocess

logger = get_logger(__name__)
mcp = FastMCP("OpenAPI")

@mcp.tool()
async def generate_api_client(service_name: str) -> str:
    """Generates the API client for a given service using Gradle."""
    # Map service_name to the corresponding Gradle task name
    # e.g., auth -> generateAuthClient
    task_name = f"generate{service_name.capitalize()}Client"
    command = ["./gradlew", task_name]
    try:
        result = subprocess.run(command, capture_output=True, text=True, check=True)
        return result.stdout
    except subprocess.CalledProcessError as e:
        logger.error(f"API client generation failed for {service_name}: {e.stderr}")
        raise Exception(f"API client generation failed for {service_name}: {e.stderr}")

if __name__ == "__main__":
    mcp.run()
