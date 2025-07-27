# Portfolio Management System - Project Structure

## 📁 Directory Organization

### Root Directory
```
portfolio assistant/
├── 🚀 MCP HTTP Server
│   ├── start_portfolio_http_server.py  # HTTP server launcher (port 8081)
│   └── mcp_persistence/             # MCP server implementation
│       ├── server/                  # Server core files
│       │   ├── portfolio_server.py # Portfolio tools (14 tools)
│       │   ├── http_portfolio_server.py  # HTTP server with FastAPI
│       │   └── tool_mapper.py       # Intelligent parameter mapper
│       ├── workflows/               # LangGraph workflows
│       ├── config/                  # Configuration & settings
│       ├── tests/                   # Test files
│       └── docs/                    # Documentation
│
├── 🗄️ Persistence Layer
│   └── persistence/                 # All database-related code
│
├── 🧪 Testing & Utilities
│   ├── test_mcp_client.py          # MCP protocol tests
│   ├── test_individual_tool.py     # Direct tool testing
│   └── config_example.py           # Configuration examples
│
├── 📊 Data & Analysis
│   ├── *.ipynb                     # Jupyter notebooks
│   └── *.csv                       # Data files
│
└── 📝 Documentation
    ├── README_MCP_SERVER.md        # Complete MCP server docs
    ├── PROJECT_STRUCTURE.md        # This file
    └── requirements.txt             # Dependencies
```

### Persistence Layer Structure
```
persistence/
├── __init__.py                      # Main exports (clean imports)
│
├── database/                        # Database connection & schema
│   ├── __init__.py                 # Database exports
│   ├── connection.py               # Core database manager
│   ├── schema.sql                  # Database schema (PostgreSQL)
│   └── docker-compose.yml          # Database setup
│
├── crud/                           # CRUD operations
│   ├── __init__.py                 # CRUD exports
│   └── portfolio_crud.py           # All portfolio CRUD operations
│
├── migrations/                     # Data migration tools
│   ├── __init__.py                 # Migration exports
│   └── migrate_csv_data.py         # CSV to database migration
│
└── demo/                          # Demo data & examples
    ├── __init__.py                 # Demo exports
    └── demo.py                     # Portfolio demonstrations
```

## 🔗 Import Structure

### ✅ Clean Imports (Recommended)
```python
# Import everything from main persistence module
from persistence import (
    get_db_manager,                 # Database connection
    transaction_crud,               # Transaction operations
    position_crud,                  # Position operations
    portfolio_crud,                 # Portfolio operations
    TransactionCreate,              # Data models
    TransactionUpdate,
    PositionUpdate
)
```

### ✅ Direct Module Imports (For specific needs)
```python
# Import from specific modules
from persistence.database import get_db_manager, DatabaseManager
from persistence.crud import TransactionCRUD, PositionCRUD
```

### ❌ Old Imports (No longer work)
```python
# These imports will fail after restructuring:
from persistence.database import ...     # File moved
from persistence.portfolio_crud import ...  # File moved
```

## 🗄️ Database Files Location

| File | New Location | Purpose |
|------|--------------|---------|
| `schema.sql` | `persistence/database/` | PostgreSQL schema definition |
| `docker-compose.yml` | `persistence/database/` | Database container setup |
| `connection.py` | `persistence/database/` | Database connection manager |

## 🛠️ Benefits of New Structure

### 🎯 **Organized by Function**
- **Database**: All connection and schema files together
- **CRUD**: All data operations in one place
- **Migrations**: Data migration tools separated
- **Demo**: Example code isolated

### 📦 **Clean Imports**
- Single import statement gets everything needed
- Clear module boundaries
- Easier to maintain and extend

### 🔧 **Better Development**
- Easy to find relevant code
- Logical grouping of related functionality
- Separate concerns (DB vs CRUD vs migrations)

### 🚀 **Production Ready**
- All database setup files in one location
- Clear separation of server code vs persistence
- Easy deployment and configuration

## 🧪 Testing the New Structure

### Database Connection Test
```bash
python -c "from persistence import get_db_manager; print('✅ DB Connected')"
```

### CRUD Operations Test
```bash
python test_individual_tool.py
```

### Full MCP Server Test
```bash
python test_mcp_client.py --simple
```

### HTTP Server Startup Test
```bash
python start_portfolio_http_server.py
```

## 📝 Migration Notes

### Files Moved:
- `database.py` → `persistence/database/connection.py`
- `init.sql` → `persistence/database/schema.sql`
- `docker-compose.yml` → `persistence/database/`
- `portfolio_crud.py` → `persistence/crud/`
- `migrate_csv_data.py` → `persistence/migrations/`
- `demo.py` → `persistence/demo/`

### Import Updates:
- ✅ All project files updated to use new imports
- ✅ All relative imports fixed in moved files
- ✅ Clean exports added via `__init__.py` files

### Backward Compatibility:
- ❌ Old direct imports will fail (intentional)
- ✅ All functionality preserved
- ✅ Same API, better organization

---

**Last Updated**: July 25, 2025  
**Structure Version**: 2.0  
**Compatibility**: All existing functionality preserved 