"""
Portfolio MCP Server with HTTP Transport
Using the official MCP Streamable HTTP transport for REST API access
"""

import asyncio
import json
import logging
from typing import Dict, Any, List
from pathlib import Path
import sys

# Add parent directory to path for imports
sys.path.insert(0, str(Path(__file__).parent.parent.parent))

# HTTP Server imports
from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import StreamingResponse, JSONResponse
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

from datetime import datetime

# HTTP Server imports
from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import StreamingResponse, JSONResponse
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

class PortfolioHTTPServer:
    """Portfolio MCP Server with HTTP Transport"""
    
    def __init__(self, host: str = "localhost", port: int = 8080):
        self.host = host
        self.port = port
        self.app = FastAPI(
            title="Portfolio MCP Server",
            description="Portfolio Management MCP Server with HTTP Transport",
            version="1.0.0"
        )
        
        # Configure CORS for security
        self.app.add_middleware(
            CORSMiddleware,
            allow_origins=["http://localhost:*", "http://127.0.0.1:*"],  # Restrict to localhost
            allow_credentials=True,
            allow_methods=["GET", "POST", "DELETE"],
            allow_headers=["*"],
        )
        
        self.setup_routes()
        self.setup_mcp_tools()
        
        # Configure logging
        logging.basicConfig(level=logging.INFO)
        self.logger = logging.getLogger(__name__)
    
    def setup_routes(self):
        """Setup HTTP routes for MCP protocol"""
        
        @self.app.get("/")
        async def root():
            """Root endpoint with server information"""
            return {
                "name": "Portfolio MCP Server",
                "version": "1.0.0",
                "protocol": "Model Context Protocol",
                "transport": "HTTP",
                "tools_count": len(self.get_available_tools()),
                "endpoints": {
                    "mcp": "/mcp",
                    "tools": "/tools",
                    "health": "/health"
                }
            }
        
        @self.app.get("/health")
        async def health_check():
            """Health check endpoint"""
            try:
                # Test database connection
                from persistence import get_db_manager
                db = get_db_manager()
                db.execute_query("SELECT 1")
                
                return {
                    "status": "healthy",
                    "database": "connected",
                    "tools": len(self.get_available_tools())
                }
            except Exception as e:
                raise HTTPException(status_code=503, detail=f"Unhealthy: {e}")
        
        @self.app.get("/tools")
        async def list_tools():
            """List all available portfolio tools"""
            tools = self.get_available_tools()
            return {
                "tools": [
                    {
                        "name": tool["name"],
                        "description": tool["description"],
                        "parameters": tool.get("parameters", [])
                    }
                    for tool in tools
                ],
                "total": len(tools)
            }
        
        @self.app.post("/tools/{tool_name}")
        async def call_tool(tool_name: str, payload: Dict[str, Any] = None):
            """Call a specific tool"""
            if payload is None:
                payload = {}
            
            try:
                result = await self.execute_tool(tool_name, payload)
                return {
                    "tool": tool_name,
                    "result": result,
                    "success": True
                }
            except Exception as e:
                raise HTTPException(status_code=400, detail=str(e))
        
        # MCP Protocol Endpoints
        @self.app.post("/mcp")
        @self.app.get("/mcp")
        async def mcp_endpoint(request: Request):
            """
            Main MCP endpoint supporting both POST and GET
            Following MCP Streamable HTTP transport specification
            """
            if request.method == "POST":
                # Handle JSON-RPC requests
                body = await request.json()
                response = await self.handle_mcp_request(body)
                
                # Return JSON response for most requests
                if isinstance(response, dict):
                    return JSONResponse(response)
                
                # Return SSE stream for requests that need it
                return StreamingResponse(
                    self.stream_mcp_response(response),
                    media_type="text/event-stream"
                )
            
            elif request.method == "GET":
                # Handle SSE stream requests
                return StreamingResponse(
                    self.mcp_sse_stream(),
                    media_type="text/event-stream"
                )
    
    async def handle_mcp_request(self, request_body: Dict[str, Any]) -> Dict[str, Any]:
        """Handle MCP JSON-RPC requests"""
        method = request_body.get("method")
        params = request_body.get("params", {})
        request_id = request_body.get("id")
        
        try:
            if method == "initialize":
                result = {
                    "protocolVersion": "2024-11-05",
                    "capabilities": {
                        "tools": {"listChanged": False},
                        "resources": {"subscribe": False, "listChanged": False},
                        "prompts": {"listChanged": False}
                    },
                    "serverInfo": {
                        "name": "portfolio-persistence",
                        "version": "1.0.0"
                    }
                }
                
            elif method == "tools/list":
                tools = self.get_available_tools()
                result = {"tools": tools}
                
            elif method == "tools/call":
                tool_name = params.get("name")
                arguments = params.get("arguments", {})
                tool_result = await self.execute_tool(tool_name, arguments)
                result = {
                    "content": [{"type": "text", "text": str(tool_result)}],
                    "isError": False
                }
                
            elif method == "resources/list":
                result = {"resources": []}  # No resources for now
                
            elif method == "prompts/list":
                result = {"prompts": []}  # No prompts for now
                
            else:
                return {
                    "jsonrpc": "2.0",
                    "id": request_id,
                    "error": {"code": -32601, "message": "Method not found"}
                }
            
            return {
                "jsonrpc": "2.0", 
                "id": request_id,
                "result": result
            }
            
        except Exception as e:
            return {
                "jsonrpc": "2.0",
                "id": request_id, 
                "error": {"code": -32603, "message": str(e)}
            }
    
    async def stream_mcp_response(self, response_data):
        """Stream MCP response as SSE"""
        yield f"data: {json.dumps(response_data)}\n\n"
    
    async def mcp_sse_stream(self):
        """Handle MCP SSE streaming"""
        # For now, just send a heartbeat
        while True:
            yield f"data: {json.dumps({'type': 'heartbeat', 'timestamp': datetime.now().isoformat()})}\n\n"
            await asyncio.sleep(30)
    
    def setup_mcp_tools(self):
        """Setup all MCP tools using our existing portfolio tools"""
        # Import our existing tools
        try:
            from .portfolio_server import PORTFOLIO_TOOLS
        except ImportError:
            # Fallback to absolute import
            from mcp_persistence.server.portfolio_server import PORTFOLIO_TOOLS
        self.portfolio_tools = PORTFOLIO_TOOLS
    
    def get_available_tools(self) -> List[Dict[str, Any]]:
        """Get list of available portfolio tools with accurate schemas"""
        from .portfolio_service import portfolio_service
        
        tools = []
        for tool in self.portfolio_tools:
            # Use the service to generate accurate schema
            schema = portfolio_service.get_tool_schema(tool.func)
            
            tool_info = {
                "name": tool.name,
                "description": tool.description or schema.get("description", ""),
                "inputSchema": {
                    "type": schema.get("type", "object"),
                    "properties": schema.get("properties", {}),
                    "required": schema.get("required", [])
                }
            }
            
            tools.append(tool_info)
        
        return tools
    
    async def execute_tool(self, tool_name: str, arguments: Dict[str, Any]) -> Any:
        """Execute a portfolio tool using automatic parameter mapping"""
        # Find the tool
        tool = None
        for t in self.portfolio_tools:
            if t.name == tool_name:
                tool = t
                break
        
        if not tool:
            raise ValueError(f"Tool '{tool_name}' not found")
        
        # Use the service to handle parameter transformation and execution
        from .portfolio_service import portfolio_service
        return await portfolio_service.execute_tool(tool.func, arguments)
    
    async def start_server(self):
        """Start the HTTP server"""
        self.logger.info(f"🚀 Starting Portfolio MCP HTTP Server on {self.host}:{self.port}")
        self.logger.info(f"📊 Available tools: {len(self.get_available_tools())}")
        self.logger.info(f"🔗 MCP Endpoint: http://{self.host}:{self.port}/mcp")
        self.logger.info(f"🛠️ Tools API: http://{self.host}:{self.port}/tools")
        self.logger.info(f"💚 Health Check: http://{self.host}:{self.port}/health")
        
        # Configure uvicorn
        config = uvicorn.Config(
            app=self.app,
            host=self.host,
            port=self.port,
            log_level="info",
            access_log=True
        )
        
        server = uvicorn.Server(config)
        await server.serve()

# Command line interface
async def main():
    """Main entry point for HTTP server"""
    import argparse
    
    parser = argparse.ArgumentParser(description="Portfolio MCP HTTP Server")
    parser.add_argument("--host", default="localhost", help="Server host")
    parser.add_argument("--port", type=int, default=8081, help="Server port")
    
    args = parser.parse_args()
    
    # Create and start server
    server = PortfolioHTTPServer(host=args.host, port=args.port)
    await server.start_server()

if __name__ == "__main__":
    asyncio.run(main()) 