# Portfolio MCP HTTP Server Usage Guide

## 🚀 **Overview**

The Portfolio MCP HTTP Server exposes all portfolio management tools through standard HTTP REST APIs, following the official **MCP Streamable HTTP transport** specification.

## 📡 **Available Endpoints**

### **Server Information**
- `GET /` - Server information and available endpoints
- `GET /health` - Health check and database status
- `GET /tools` - List all available portfolio tools

### **Tool Execution**
- `POST /tools/{tool_name}` - Execute a specific portfolio tool

### **MCP Protocol** 
- `POST /mcp` - MCP JSON-RPC requests (initialize, tools/list, tools/call)
- `GET /mcp` - MCP Server-Sent Events stream

## 🛠️ **Starting the Server**

### **Basic Startup**
```bash
# Start with default settings (localhost:8081)
python start_portfolio_http_server.py

# Or start directly
cd mcp_persistence/server
python http_portfolio_server.py
```

### **Custom Host/Port**
```bash
# Custom host and port
python mcp_persistence/server/http_portfolio_server.py --host 0.0.0.0 --port 8080
```

### **Expected Output**
```
🚀 Starting Portfolio MCP HTTP Server on localhost:8081
📊 Available tools: 14
🔗 MCP Endpoint: http://localhost:8081/mcp
🛠️ Tools API: http://localhost:8081/tools
💚 Health Check: http://localhost:8081/health
INFO:     Started server process [12345]
INFO:     Waiting for application startup.
INFO:     Application startup complete.
INFO:     Uvicorn running on http://localhost:8080 (Press CTRL+C to quit)
```

## 🧪 **Testing with curl**

### **1. Server Health Check**
```bash
curl -X GET http://localhost:8081/health
```

**Response:**
```json
{
  "status": "healthy",
  "database": "connected", 
  "tools": 14
}
```

### **2. Server Information**
```bash
curl -X GET http://localhost:8081/
```

**Response:**
```json
{
  "name": "Portfolio MCP Server",
  "version": "1.0.0",
  "protocol": "Model Context Protocol",
  "transport": "HTTP",
  "tools_count": 14,
  "endpoints": {
    "mcp": "/mcp",
    "tools": "/tools", 
    "health": "/health"
  }
}
```

### **3. List All Available Tools**
```bash
curl -X GET http://localhost:8081/tools
```

**Response:**
```json
{
  "tools": [
    {
      "name": "get_portfolio_summary_tool",
      "description": "Get comprehensive portfolio summary with key metrics",
      "parameters": []
    },
    {
      "name": "create_transaction_tool",
      "description": "Create a new buy/sell transaction in the portfolio", 
      "parameters": ["ticker", "transaction_type", "quantity", "cost_per_share"]
    },
    {
      "name": "get_all_positions_tool",
      "description": "Get all current positions in the portfolio",
      "parameters": []
    }
    // ... 11 more tools
  ],
  "total": 14
}
```

## 🔧 **Complete Tool Execution Examples**

> **✨ Smart Parameter Mapping**: The server uses an intelligent parameter mapper that automatically handles both individual parameters and structured Pydantic models. You don't need to worry about the internal parameter structure - just send the data as JSON!

### **1. Create Transaction**
Create a new buy/sell transaction in the portfolio.

```bash
curl -X POST http://localhost:8081/tools/create_transaction_tool \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL",
    "transaction_type": "BUY", 
    "quantity": 10,
    "cost_per_share": 150.25,
    "transaction_date": "2024-01-15",
    "currency": "USD",
    "commission": 9.99,
    "notes": "Initial AAPL position"
  }'
```

### **2. Get Transaction by ID**
Retrieve a specific transaction by its ID.

```bash
curl -X POST http://localhost:8081/tools/get_transaction_tool \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": 1234
  }'
```

### **3. Update Transaction**
Update an existing transaction's details.

```bash
curl -X POST http://localhost:8081/tools/update_transaction_tool \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": 1234,
    "quantity": 12,
    "cost_per_share": 148.50,
    "notes": "Updated quantity and price"
  }'
```

### **4. Delete Transaction**
Delete a transaction by its ID.

```bash
curl -X POST http://localhost:8081/tools/delete_transaction_tool \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": 1234
  }'
```

### **5. Get Transactions by Ticker**
Get all transactions for a specific stock ticker.

```bash
curl -X POST http://localhost:8081/tools/get_transactions_by_ticker_tool \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL"
  }'
```

### **6. Get All Positions**
Get all current positions in the portfolio.

```bash
curl -X POST http://localhost:8081/tools/get_all_positions_tool \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Response:**
```json
{
  "tool": "get_all_positions_tool",
  "result": [
    {
      "ticker": "AAPL",
      "quantity": 10,
      "avg_cost": 150.25,
      "current_price": 175.30,
      "total_value": 1753.00,
      "unrealized_gain_loss": 250.50
    },
    {
      "ticker": "GOOGL", 
      "quantity": 5,
      "avg_cost": 2800.00,
      "current_price": 2950.00,
      "total_value": 14750.00,
      "unrealized_gain_loss": 750.00
    }
  ],
  "success": true
}
```

### **7. Get Position by Ticker**
Get position details for a specific ticker.

```bash
curl -X POST http://localhost:8081/tools/get_position_by_ticker_tool \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL"
  }'
```

### **8. Update Market Data**
Update current market price for a position.

```bash
curl -X POST http://localhost:8081/tools/update_market_data_tool \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL",
    "current_price": 180.00
  }'
```

### **9. Get Portfolio Summary**
Get comprehensive portfolio summary with key metrics.

```bash
curl -X POST http://localhost:8081/tools/get_portfolio_summary_tool \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Response:**
```json
{
  "tool": "get_portfolio_summary_tool",
  "result": {
    "total_value": 125450.00,
    "positions_count": 8,
    "cash_available": 2340.00,
    "total_return_pct": 12.5,
    "daily_change_pct": 0.8
  },
  "success": true
}
```

### **10. Get Ticker Analysis**
Get comprehensive analysis for a specific ticker.

```bash
curl -X POST http://localhost:8081/tools/get_ticker_analysis_tool \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL"
  }'
```

### **11. Search Transactions**
Search transactions with advanced filters.

```bash
curl -X POST http://localhost:8081/tools/search_transactions_tool \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL",
    "start_date": "2024-01-01",
    "end_date": "2024-12-31",
    "transaction_type": "BUY",
    "min_quantity": 5,
    "max_quantity": 100
  }'
```

### **12. Get Performance Metrics**
Get portfolio performance and risk metrics.

```bash
curl -X POST http://localhost:8081/tools/get_performance_metrics_tool \
  -H "Content-Type: application/json" \
  -d '{}'
```

### **13. Recalculate Position**
Recalculate position for a specific ticker.

```bash
curl -X POST http://localhost:8081/tools/recalculate_position_tool \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL"
  }'
```

### **14. Recalculate All Positions**
Recalculate all positions in the portfolio.

```bash
curl -X POST http://localhost:8081/tools/recalculate_all_positions_tool \
  -H "Content-Type: application/json" \
  -d '{}'
```

## 🔗 **MCP JSON-RPC Protocol**

### **Initialize Connection**
```bash
curl -X POST http://localhost:8081/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize", 
    "params": {
      "protocolVersion": "2024-11-05",
      "capabilities": {},
      "clientInfo": {"name": "curl-client", "version": "1.0.0"}
    }
  }'
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "protocolVersion": "2024-11-05",
    "capabilities": {
      "tools": {"listChanged": false},
      "resources": {"subscribe": false, "listChanged": false}, 
      "prompts": {"listChanged": false}
    },
    "serverInfo": {
      "name": "portfolio-persistence",
      "version": "1.0.0"
    }
  }
}
```

### **List Tools via MCP**
```bash
curl -X POST http://localhost:8081/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0", 
    "id": 2,
    "method": "tools/list",
    "params": {}
  }'
```

### **Call Tool via MCP**
```bash
curl -X POST http://localhost:8081/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 3,
    "method": "tools/call",
    "params": {
      "name": "get_portfolio_summary_tool",
      "arguments": {}
    }
  }'
```

## 🔍 **Error Handling**

### **Tool Not Found**
```bash
curl -X POST http://localhost:8081/tools/nonexistent_tool \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Response:**
```json
{
  "detail": "Tool 'nonexistent_tool' not found"
}
```

### **Invalid Parameters**
```bash
curl -X POST http://localhost:8081/tools/create_transaction_tool \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL"
    // Missing required parameters
  }'
```

**Response:**
```json
{
  "detail": "Tool execution failed: missing required parameter 'transaction_type'"
}
```

## 🌐 **Integration with LangChain/LangGraph**

### **LangChain HTTP Tool Integration**
```python
from langchain.tools import Tool
import requests

def call_portfolio_tool(tool_name: str, **kwargs):
    """Call portfolio tool via HTTP"""
    response = requests.post(
        f"http://localhost:8081/tools/{tool_name}",
        json=kwargs
    )
    return response.json()["result"]

# Create LangChain tools
portfolio_summary = Tool(
    name="portfolio_summary",
    description="Get portfolio summary",
    func=lambda: call_portfolio_tool("get_portfolio_summary_tool")
)

get_positions = Tool(
    name="get_positions", 
    description="Get all portfolio positions",
    func=lambda: call_portfolio_tool("get_all_positions_tool")
)
```

### **Using with OpenAI Function Calling**
```python
import openai
import requests

# Define function schema
tools = [
    {
        "type": "function",
        "function": {
            "name": "get_portfolio_summary",
            "description": "Get portfolio summary with key metrics",
            "parameters": {
                "type": "object",
                "properties": {},
                "required": []
            }
        }
    }
]

def execute_portfolio_function(function_name, arguments):
    """Execute portfolio function via HTTP API"""
    tool_name = f"{function_name}_tool"
    response = requests.post(
        f"http://localhost:8081/tools/{tool_name}",
        json=arguments
    )
    return response.json()["result"]

# Use with OpenAI
response = openai.chat.completions.create(
    model="gpt-4",
    messages=[{"role": "user", "content": "Show me my portfolio summary"}],
    tools=tools
)
```

## 🛡️ **Security Considerations**

### **CORS Configuration**
- Server allows connections from `localhost` and `127.0.0.1` only
- Configure `allow_origins` for production use

### **Database Security**
- Ensure database credentials are properly secured
- Use environment variables for sensitive configuration

### **Network Security**
- Default binding to `localhost` for development
- Use `--host 0.0.0.0` carefully in production
- Consider implementing authentication for production deployments

## 🚀 **Production Deployment**

### **Using Docker**
```dockerfile
FROM python:3.11
WORKDIR /app
COPY . .
RUN pip install -r mcp_persistence/config/requirements.txt
EXPOSE 8081
CMD ["python", "start_portfolio_http_server.py", "--host", "0.0.0.0"]
```

### **Using systemd Service**
```ini
[Unit]
Description=Portfolio MCP HTTP Server
After=network.target

[Service]
Type=simple
User=portfolio
WorkingDirectory=/opt/portfolio-mcp
ExecStart=/usr/bin/python3 start_portfolio_http_server.py
Restart=always

[Install] 
WantedBy=multi-user.target
```

---

## 🎯 **Next Steps**

1. **Start the server**: `python start_portfolio_http_server.py`
2. **Test health**: `curl http://localhost:8081/health`
3. **List tools**: `curl http://localhost:8081/tools`
4. **Execute tools**: Use the curl examples above
5. **Integrate with your application**: Use HTTP requests or MCP protocol

Your Portfolio MCP Server is now accessible via standard HTTP APIs! 🎉 