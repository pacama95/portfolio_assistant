# Portfolio MCP Server Architecture

## Overview

The Portfolio MCP (Model Context Protocol) Server implements a clean, layered architecture that separates concerns and follows best practices for API development. The system is organized into distinct layers that handle different responsibilities.

## Architecture Layers

### 🎮 **Controller Layer**
**File**: `server/http_portfolio_server.py`

**Responsibilities:**
- Handle HTTP requests and responses
- Implement MCP protocol compliance
- Route requests to appropriate services
- Manage CORS and security policies
- Transform external API calls to internal service calls

**Key Features:**
- FastAPI-based HTTP server
- MCP Streamable HTTP transport support
- RESTful endpoints for portfolio operations
- Health checks and monitoring
- API documentation generation

### 🔧 **Service Layer**
**File**: `server/portfolio_service.py` (formerly `tool_mapper.py`)

**Responsibilities:**
- Parameter transformation and validation
- Business logic execution
- Act as adapter between controller and data layers
- Tool schema generation for API documentation
- Input/output data mapping

**Key Features:**
- Automatic parameter mapping between HTTP requests and internal functions
- Support for both individual parameters and structured Pydantic models
- Runtime tool analysis and schema generation
- Type-safe parameter validation
- Async/sync function execution handling

### 📊 **Domain/Business Logic Layer**
**File**: `server/portfolio_server.py`

**Responsibilities:**
- Define portfolio-specific business operations
- Implement portfolio calculation logic
- Expose tool interfaces for external consumption
- Handle domain-specific validation rules

**Key Features:**
- MCP-compliant tool definitions
- Portfolio analysis functions
- Transaction management operations
- Position calculation and tracking
- Market data integration

### 💾 **Data/Persistence Layer**
**Directory**: `../persistence/`

**Responsibilities:**
- Database connection management
- CRUD operations for transactions and positions
- Data model definitions
- Database schema management
- Query optimization

**Key Components:**
- `database/connection.py` - Database connection and configuration
- `crud/portfolio_crud.py` - Data access operations
- `database/schema.sql` - Database schema definition
- `migrations/` - Data migration utilities

## Project Structure

The clean, organized structure reflects the layered architecture:

```
📁 mcp_persistence/                     # MCP Server Implementation
├── 📁 server/                         # Application layers
│   ├── 🎮 http_portfolio_server.py    # Controller Layer
│   ├── 🔧 portfolio_service.py        # Service Layer  
│   └── 📊 portfolio_server.py         # Domain/Business Logic Layer
└── 📁 docs/                           # Documentation
    └── 📋 ARCHITECTURE.md             # Architecture documentation

📁 persistence/                         # Data/Persistence Layer
├── 📁 database/                       # Database management
│   ├── 🐘 schema.sql                  # Database schema
│   ├── 🐳 docker-compose.yml          # Infrastructure setup
│   ├── 🔌 connection.py               # Connection management
│   ├── 🧪 test_db_setup.py           # Database testing
│   └── 📋 README.md                   # Database documentation
├── 📁 crud/                           # Data access operations
│   └── 📊 portfolio_crud.py           # CRUD operations
└── 📁 migrations/                     # Data migration utilities
    └── 📥 migrate_csv_data.py          # CSV data migration

📁 Root Level Files
├── 🧪 test_portfolio_service.py       # Service layer tests
├── 🚀 start_portfolio_http_server.py  # Server startup script
└── 📋 requirements.txt                # Python dependencies
```

## Component Interactions

```
HTTP Request → Controller → Service → Domain Logic → Data Layer → Database
     ↓              ↓           ↓           ↓            ↓
   FastAPI    → Parameter → Business → CRUD Ops → PostgreSQL
            Mapping      Logic
```

### Request Flow

1. **HTTP Request** arrives at the controller layer
2. **Controller** validates request format and routes to appropriate endpoint
3. **Service Layer** transforms request parameters and validates input
4. **Domain Logic** executes business operations using validated data
5. **Data Layer** performs database operations through CRUD interfaces
6. **Response** flows back through the layers with appropriate transformations

## Design Patterns

### 🔄 **Adapter Pattern**
The service layer acts as an adapter between the MCP protocol interface and the internal business logic, handling parameter transformation and validation.

### 🏭 **Factory Pattern**  
Tool registration and execution use factory patterns to dynamically create and execute portfolio operations.

### 📦 **Repository Pattern**
The persistence layer implements repository patterns for data access, abstracting database operations behind clean interfaces.

### 🎯 **Dependency Injection**
Services receive their dependencies through constructor injection, making the system testable and maintainable.

## Configuration Management

### Environment Variables
```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=portfolio_db
DB_USER=portfolio_user
DB_PASSWORD=portfolio_password

# Server Configuration  
HTTP_HOST=localhost
HTTP_PORT=8081
LOG_LEVEL=INFO
```

### Configuration Files
- Environment variables for database and server configuration
- `persistence/database/docker-compose.yml` - Infrastructure setup
- `requirements.txt` - Python dependencies

## API Endpoints

### MCP Protocol Endpoints
- `POST /mcp` - Main MCP JSON-RPC endpoint
- `GET /mcp` - MCP Server-Sent Events stream

### Portfolio API Endpoints  
- `GET /` - Server information and capabilities
- `GET /health` - Health check and status
- `GET /tools` - List available portfolio tools
- `POST /tools/{tool_name}` - Execute specific portfolio tool

### Tool Categories

**Portfolio Analysis:**
- `get_portfolio_summary_tool` - Overall portfolio metrics
- `get_all_positions_tool` - Current position holdings
- `get_performance_metrics_tool` - Performance analysis

**Transaction Management:**
- `create_transaction_tool` - Add new transactions
- `get_transactions_by_ticker_tool` - Retrieve ticker transactions
- `search_transactions_tool` - Advanced transaction search

**Position Tracking:**
- `get_position_by_ticker_tool` - Individual position details
- `update_market_price_tool` - Update current market prices
- `recalculate_position_tool` - Recalculate position metrics

## Testing Strategy

### Unit Tests
- **Service Layer**: Test parameter mapping and validation logic
- **Domain Logic**: Test business rule implementation
- **Data Layer**: Test CRUD operations and database interactions

### Integration Tests
- **API Endpoints**: Test complete request/response cycles
- **Database Integration**: Test data persistence and retrieval
- **MCP Protocol**: Test protocol compliance and communication

### Test Files
- `test_portfolio_service.py` - Service layer testing
- `tests/test_tools.py` - Tool functionality testing
- `persistence/database/test_db_setup.py` - Database setup verification

## Deployment Architecture

### Development Environment
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   HTTP Client   │───▶│  FastAPI Server  │───▶│   PostgreSQL    │
│  (Cursor, etc.) │    │   (Port 8081)    │    │   (Port 5432)   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌──────────────────┐
                       │     Adminer      │
                       │   (Port 8080)    │
                       └──────────────────┘
```

### Production Considerations
- **Load Balancing**: Multiple server instances behind a load balancer
- **Database Clustering**: PostgreSQL with read replicas
- **Caching**: Redis for frequently accessed data
- **Monitoring**: Prometheus/Grafana for metrics and alerting
- **Security**: JWT authentication, rate limiting, SSL/TLS

## Security Implementation

### Input Validation
- Pydantic models for request/response validation
- SQL injection prevention through parameterized queries
- Type checking and data sanitization

### Access Control
- CORS policies for API access control
- Request rate limiting to prevent abuse
- Environment-based configuration for sensitive data

### Data Protection
- Database connection encryption
- Secure password storage
- Audit logging for data modifications

## Performance Optimization

### Database Optimization
- Proper indexing for query performance
- Connection pooling for database efficiency
- Prepared statements for repeated queries

### Caching Strategy
- Schema caching for tool definitions
- Connection pooling for database access
- Response caching for expensive calculations

### Async Processing
- Async/await patterns for I/O operations
- Non-blocking database operations
- Concurrent request handling

## Monitoring and Observability

### Health Checks
- Database connectivity monitoring
- Service dependency checks
- Performance metrics collection

### Logging Strategy
- Structured logging with proper levels
- Request/response logging for debugging
- Error tracking and alerting

### Metrics Collection
- Request latency and throughput
- Database query performance
- Error rates and patterns

## Future Enhancements

### Planned Features
- Real-time market data integration
- Advanced portfolio analytics
- Multi-currency support expansion
- Performance benchmarking tools

### Scalability Improvements
- Microservices architecture migration
- Event-driven architecture for updates
- Horizontal scaling capabilities
- Improved caching strategies

---

This architecture provides a solid foundation for portfolio management operations while maintaining flexibility for future enhancements and scalability requirements. 