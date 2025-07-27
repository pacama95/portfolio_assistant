#!/usr/bin/env python3
"""
Portfolio MCP HTTP Server Launcher
Starts the portfolio management server with HTTP transport
"""

import sys
import asyncio
from pathlib import Path

# Add current directory to path for imports
sys.path.insert(0, str(Path(__file__).parent))

async def main():
    """Main entry point for HTTP server launcher"""
    try:
        from mcp_persistence.server.http_portfolio_server import main as http_main
        print("🚀 Starting Portfolio MCP HTTP Server...")
        await http_main()
    except KeyboardInterrupt:
        print("\n👋 Portfolio MCP HTTP Server stopped by user")
    except Exception as e:
        print(f"❌ Error starting HTTP server: {e}")
        return 1
    return 0

if __name__ == "__main__":
    exit_code = asyncio.run(main())
    sys.exit(exit_code) 