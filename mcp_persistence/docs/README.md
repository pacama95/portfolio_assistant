# Portfolio MCP Server

A comprehensive Model Context Protocol (MCP) server for portfolio management with LangChain/LangGraph integration.

## 🚀 Features

### Core Capabilities
- **Complete CRUD Operations**: Full transaction and position management
- **Advanced Workflows**: LangGraph-powered transaction validation, portfolio analysis, and rebalancing
- **Real-time Portfolio Analytics**: Performance metrics, risk assessment, and comprehensive reporting
- **Multi-currency Support**: Handle global portfolios with currency-specific operations
- **Robust Error Handling**: Comprehensive validation and retry mechanisms

### Available Tools (14 total)

#### Transaction Management
- `create_transaction_tool`: Create new buy/sell transactions
- `get_transaction_tool`: Retrieve transaction by ID
- `update_transaction_tool`: Modify existing transactions
- `delete_transaction_tool`: Remove transactions
- `get_transactions_by_ticker_tool`: Get all transactions for a ticker
- `search_transactions_tool`: Advanced transaction search with filters

#### Position Management
- `get_all_positions_tool`: Retrieve all current positions
- `get_position_by_ticker_tool`: Get position details for specific ticker
- `update_market_data_tool`: Update current market prices
- `recalculate_position_tool`: Recalculate position for specific ticker
- `recalculate_all_positions_tool`: Recalculate all positions

#### Portfolio Analytics
- `get_portfolio_summary_tool`: Key portfolio metrics and summary
- `get_ticker_analysis_tool`: Comprehensive analysis for specific ticker
- `get_performance_metrics_tool`: Portfolio performance and risk metrics

### LangGraph Workflows

#### Transaction Workflow
- **Validation Node**: Comprehensive data validation
- **Execution Node**: Safe transaction processing
- **Error Handling**: Automatic retry and rollback

#### Portfolio Analysis Workflow  
- **Multi-type Analysis**: Full, performance, position, or ticker-specific
- **Parallel Processing**: Concurrent data gathering
- **Risk Assessment**: Automatic risk level calculation

#### Rebalancing Workflow
- **Risk Assessment**: Pre-rebalancing safety checks
- **Action Generation**: Specific buy/sell recommendations
- **Threshold Monitoring**: Only rebalance when deviation > 1%

## 📋 Prerequisites

### System Requirements
- Python 3.8+
- PostgreSQL 12+
- 4GB RAM minimum (8GB recommended)

### Required Dependencies
```bash
pip install -r requirements.txt
```

Key packages:
- `mcp>=1.0.0`: Model Context Protocol
- `langchain>=0.1.0`: AI framework
- `langgraph>=0.0.20`: Workflow orchestration
- `psycopg2-binary>=2.9.0`: PostgreSQL adapter
- `pydantic>=1.10.0`: Data validation

## 🛠️ Installation & Setup

### 1. Install Dependencies
```bash
pip install -r requirements.txt
```

### 2. Database Configuration
Set environment variables:
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=portfolio_db
export DB_USER=postgres
export DB_PASSWORD=your_password
```

### 3. Initialize Database
Ensure your database schema is set up (use the existing `init.sql` and migration scripts).

### 4. Configure Logging (Optional)
```bash
export LOG_LEVEL=INFO
export LOG_FILE=/var/log/portfolio_mcp.log
```

## 🚀 Usage

### Starting the HTTP Server
```bash
# Method 1: Direct execution (default: localhost:8081)
python ../start_portfolio_http_server.py

# Method 2: With custom configuration
python server/http_portfolio_server.py --host 0.0.0.0 --port 8080

# Method 3: Background service
nohup python ../start_portfolio_http_server.py > server.log 2>&1 &
```

### Server Banner
```
╔══════════════════════════════════════════════════════════════╗
║                   Portfolio MCP Server                      ║
║                      Version 1.0.0                        ║
║                                                              ║
║  🔧 Tools Available: 14 portfolio management tools          ║
║  🔄 Workflows: Transaction, Analysis, Rebalancing           ║
║  🗄️  Database: PostgreSQL                                   ║
║  🧠 AI Integration: LangChain + LangGraph                   ║
╚══════════════════════════════════════════════════════════════╝
```

## 🔧 Tool Usage Examples

### Creating a Transaction
```json
{
  "tool": "create_transaction_tool",
  "parameters": {
    "ticker": "AAPL",
    "transaction_type": "BUY",
    "quantity": 100,
    "cost_per_share": 150.25,
    "currency": "USD",
    "transaction_date": "2024-01-15",
    "commission": 9.95,
    "notes": "Long-term investment"
  }
}
```

### Portfolio Analysis
```json
{
  "tool": "get_ticker_analysis_tool",
  "parameters": {
    "ticker": "AAPL"
  }
}
```

### Market Data Update
```json
{
  "tool": "update_market_data_tool",
  "parameters": {
    "ticker": "AAPL",
    "current_price": 175.50
  }
}
```

### Advanced Transaction Search
```json
{
  "tool": "search_transactions_tool",
  "parameters": {
    "start_date": "2024-01-01",
    "end_date": "2024-12-31",
    "transaction_type": "BUY",
    "min_quantity": 50
  }
}
```

## 🔄 Workflow Examples

### Transaction Workflow
```python
from portfolio_workflows import workflow_manager

# Execute transaction with validation
result = await workflow_manager.execute_transaction_workflow(
    operation="create",
    transaction_data={
        "ticker": "MSFT",
        "transaction_type": "BUY",
        "quantity": 50,
        "cost_per_share": 300.00,
        "transaction_date": "2024-01-15"
    }
)
```

### Portfolio Analysis Workflow
```python
# Full portfolio analysis
result = await workflow_manager.execute_analysis_workflow(
    analysis_type="full"
)

# Ticker-specific analysis
result = await workflow_manager.execute_analysis_workflow(
    analysis_type="ticker",
    ticker="AAPL"
)
```

### Rebalancing Workflow
```python
# Portfolio rebalancing
result = await workflow_manager.execute_rebalancing_workflow(
    current_positions=current_positions,
    target_allocation={
        "AAPL": 30.0,  # 30%
        "MSFT": 25.0,  # 25%
        "GOOGL": 20.0, # 20%
        "AMZN": 25.0   # 25%
    }
)
```

## ⚙️ Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | localhost | Database host |
| `DB_PORT` | 5432 | Database port |
| `DB_NAME` | portfolio_db | Database name |
| `DB_USER` | postgres | Database user |
| `DB_PASSWORD` | password | Database password |
| `LOG_LEVEL` | INFO | Logging level |
| `LOG_FILE` | None | Log file path |

### Configuration Classes

The server uses structured configuration through `mcp_config.py`:

- **DatabaseConfig**: Database connection settings
- **MCPServerConfig**: Server behavior settings
- **LoggingConfig**: Logging configuration
- **WorkflowConfig**: Workflow behavior settings

## 🔍 Error Handling

### Error Types

1. **ValidationError**: Input data validation failures
2. **DatabaseError**: Database operation issues
3. **WorkflowError**: Workflow execution problems
4. **ToolExecutionError**: Tool-specific errors

### Error Response Format
```json
{
  "error_type": "validation_error",
  "field": "ticker",
  "message": "Ticker must be a non-empty string",
  "value": "",
  "suggestions": ["Use uppercase format (e.g., AAPL)", "Check if ticker exists"]
}
```

### Retry Mechanism
The server includes automatic retry logic:
- **Max Retries**: 3 attempts
- **Retry Delay**: 1 second (configurable)
- **Applicable Operations**: Database operations, workflow nodes

## 📊 Monitoring & Logging

### Log Levels
- **DEBUG**: Detailed execution information
- **INFO**: General operational messages
- **WARNING**: Validation issues and recoverable errors
- **ERROR**: Operation failures requiring attention

### Key Metrics Logged
- Tool execution times
- Database operation performance
- Workflow completion status
- Error frequencies
- Connection status

## 🧪 Testing

### Basic Server Test
```bash
# Test dependencies
python -c "import mcp, langchain, langgraph, psycopg2; print('All dependencies OK')"

# Test database connection
python -c "from persistence.database import get_db_manager; get_db_manager().execute_query('SELECT 1')"

# Test HTTP server startup
python ../start_portfolio_http_server.py
```

### Tool Testing
```python
# Test individual tools
from mcp_portfolio_server import get_all_positions_tool

result = get_all_positions_tool()
print(result)
```

## 🔒 Security Considerations

### Database Security
- Use connection pooling
- Parameterized queries (SQL injection prevention)
- Connection timeout settings
- Environment variable for credentials

### Input Validation
- Comprehensive Pydantic model validation
- Type checking and range validation
- Sanitization of ticker symbols and text inputs

### Error Information
- Sensitive information not exposed in error messages
- Detailed logging for debugging (but not in responses)

## 📈 Performance Optimization

### Database Optimization
- Connection pooling through `database.py`
- Prepared statements for frequent queries
- Indexed columns for fast lookups

### Workflow Optimization
- Parallel tool execution where possible
- Lazy loading of expensive operations
- Caching of frequently accessed data

### Memory Management
- Streaming large result sets
- Automatic cleanup of workflow state
- Connection resource management

## 🔄 Integration Examples

### With LangChain Agents
```python
from langchain.agents import AgentExecutor
from mcp_portfolio_server import PORTFOLIO_TOOLS

# Create agent with portfolio tools
agent = AgentExecutor(
    agent=agent_instance,
    tools=PORTFOLIO_TOOLS,
    verbose=True
)

# Agent can now use portfolio tools
result = agent.run("Show me my current portfolio performance")
```

### With Custom Applications
```python
import asyncio
from mcp_portfolio_server import PortfolioMCPServer

# Custom application integration
async def main():
    server = PortfolioMCPServer()
    
    # Direct tool access
    positions = await server.tools[5].func()  # get_all_positions_tool
    print(f"Current positions: {positions}")

asyncio.run(main())
```

## 🛠️ Troubleshooting

### Common Issues

#### 1. Database Connection Failed
```bash
# Check database status
pg_isready -h localhost -p 5432

# Verify credentials
psql -h localhost -p 5432 -U postgres -d portfolio_db -c "SELECT 1;"
```

#### 2. Import Errors
```bash
# Reinstall dependencies
pip install -r requirements.txt --force-reinstall

# Check Python path
python -c "import sys; print(sys.path)"
```

#### 3. Tool Execution Errors
- Check database schema matches expected structure
- Verify transaction data format
- Review logs for detailed error information

#### 4. Memory Issues
- Monitor workflow state accumulation
- Check for connection leaks
- Consider increasing system memory

### Log Analysis
```bash
# Monitor real-time logs
tail -f /var/log/portfolio_mcp.log

# Search for errors
grep -i error /var/log/portfolio_mcp.log

# Performance analysis
grep -i "execution time" /var/log/portfolio_mcp.log
```

## 📚 API Reference

### Tool Schemas
Each tool includes comprehensive Pydantic schemas for validation. See the tool definitions in `mcp_portfolio_server.py` for detailed parameter specifications.

### Workflow States
- **TransactionWorkflowState**: Transaction operation state
- **PortfolioAnalysisState**: Analysis operation state  
- **PortfolioRebalanceState**: Rebalancing operation state

## 🤝 Contributing

### Development Setup
1. Clone repository
2. Install development dependencies: `pip install -r requirements.txt`
3. Set up pre-commit hooks
4. Run tests: `pytest`

### Adding New Tools
1. Define Pydantic input model
2. Create tool function with `@tool` decorator
3. Add to `PORTFOLIO_TOOLS` list
4. Update documentation

### Workflow Extensions
1. Define new state TypedDict
2. Create workflow nodes
3. Add to workflow manager
4. Include comprehensive error handling

## 📝 License

This project is part of the portfolio management system. See the main project license for details.

## 📞 Support

For issues and questions:
1. Check the troubleshooting section
2. Review logs for detailed error information
3. Consult the existing CRUD operations documentation
4. Test individual components in isolation

---

**Version**: 1.0.0  
**Last Updated**: January 2024  
**Compatibility**: Python 3.8+, PostgreSQL 12+ 