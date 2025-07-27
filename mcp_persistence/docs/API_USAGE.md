# Portfolio Management REST API

This document describes how to use the Portfolio Management REST API that provides access to all portfolio CRUD operations.

## Architecture

The system is now organized into three layers:

1. **`portfolio_operations.py`** - Core business logic and data models
2. **`portfolio_server.py`** - LangChain tools wrapper (for LLM integration)
3. **`portfolio_api.py`** - FastAPI REST API (for HTTP access)

## Starting the API Server

### Method 1: Using the launcher script
```bash
cd mcp_persistence
python start_api_server.py

# With custom host/port
python start_api_server.py --host 127.0.0.1 --port 8080

# With auto-reload for development
python start_api_server.py --reload
```

### Method 2: Direct uvicorn command
```bash
cd mcp_persistence
uvicorn server.portfolio_api:app --host 0.0.0.0 --port 8000 --reload
```

## API Documentation

Once the server is running, you can access:

- **Interactive API docs**: http://localhost:8000/docs
- **Alternative docs**: http://localhost:8000/redoc
- **Health check**: http://localhost:8000/health

## API Endpoints

### Transaction Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/transactions` | Create a new transaction |
| GET | `/transactions/{transaction_id}` | Get transaction by ID |
| PUT | `/transactions` | Update a transaction |
| DELETE | `/transactions/{transaction_id}` | Delete a transaction |
| GET | `/transactions/ticker/{ticker}` | Get all transactions for a ticker |
| POST | `/transactions/search` | Search transactions with filters |

### Position Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/positions` | Get all positions |
| GET | `/positions/{ticker}` | Get position for a ticker |
| PUT | `/positions/market-data` | Update market data |
| POST | `/positions/{ticker}/recalculate` | Recalculate position |
| POST | `/positions/recalculate-all` | Recalculate all positions |

### Portfolio Analysis

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/portfolio/summary` | Get portfolio summary |
| GET | `/portfolio/performance` | Get performance metrics |
| GET | `/analysis/{ticker}` | Get ticker analysis |

## Example Usage

### Create a Transaction
```bash
curl -X POST "http://localhost:8000/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL",
    "transaction_type": "BUY",
    "quantity": 10,
    "cost_per_share": 150.0,
    "currency": "USD",
    "transaction_date": "2024-01-15",
    "commission": 1.0,
    "drip_confirmed": false,
    "notes": "Initial purchase"
  }'
```

### Get All Positions
```bash
curl -X GET "http://localhost:8000/positions"
```

### Search Transactions
```bash
curl -X POST "http://localhost:8000/transactions/search" \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL",
    "start_date": "2024-01-01",
    "end_date": "2024-12-31",
    "transaction_type": "BUY"
  }'
```

### Update Market Data
```bash
curl -X PUT "http://localhost:8000/positions/market-data" \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL",
    "current_price": 175.50
  }'
```

## Response Format

All API responses follow this format:

```json
{
  "success": true,
  "message": "Operation completed successfully", 
  "data": { /* response data */ }
}
```

Error responses:
```json
{
  "detail": "Error message describing what went wrong"
}
```

## Integration with LangChain Tools

The LangChain tools in `portfolio_server.py` now use the same operations as the REST API, ensuring consistency between LLM integration and direct HTTP access.

Both interfaces share:
- Same input validation models
- Same business logic
- Same error handling
- Same data transformation

## Development

The modular architecture makes it easy to:

1. **Add new operations**: Add to `portfolio_operations.py`, then expose via both API and tools
2. **Modify business logic**: Change only in `portfolio_operations.py`
3. **Test operations**: Test the core operations independently
4. **Extend interfaces**: Add new ways to access operations (GraphQL, gRPC, etc.)

## Dependencies

Make sure you have the required dependencies installed:

```bash
pip install fastapi uvicorn
```

Or install from requirements.txt:
```bash
pip install -r requirements.txt
``` 