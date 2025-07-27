"""
Persistence layer for Portfolio Management System
Provides database connections, CRUD operations, migrations, and utilities
"""

# Database connection and core utilities
from .database import (
    get_db_manager,
    DatabaseManager,
    TransactionCreate,
    TransactionUpdate,
    PositionUpdate
)

# CRUD operations
from .crud import (
    TransactionCRUD,
    PositionCRUD,
    PortfolioCRUD,
    transaction_crud,
    position_crud,
    portfolio_crud,
    create_transaction,
    get_all_positions,
    get_portfolio_summary,
    update_market_price
)

__all__ = [
    # Database
    'get_db_manager',
    'DatabaseManager',
    'TransactionCreate', 
    'TransactionUpdate',
    'PositionUpdate',
    
    # CRUD Operations
    'TransactionCRUD',
    'PositionCRUD',
    'PortfolioCRUD', 
    'transaction_crud',
    'position_crud',
    'portfolio_crud',
    'create_transaction',
    'get_all_positions',
    'get_portfolio_summary',
    'update_market_price'
] 