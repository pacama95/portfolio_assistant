"""
Portfolio Management Tools
Provides LangChain tools for all portfolio CRUD operations
"""

import json
import logging

# LangChain imports
from langchain.tools import tool

# Local imports - use operations from the new module
try:
    from portfolio_operations import (
        CreateTransactionInput,
        UpdateTransactionInput, 
        UpdateMarketDataInput,
        SearchTransactionsInput
    )
    import portfolio_operations
except ImportError:
    # Fallback to absolute import
    try:
        from server.portfolio_operations import (
            CreateTransactionInput,
            UpdateTransactionInput, 
            UpdateMarketDataInput,
            SearchTransactionsInput
        )
        from server import portfolio_operations
    except ImportError:
        # Final fallback
        from mcp_persistence.server.portfolio_operations import (
            CreateTransactionInput,
            UpdateTransactionInput, 
            UpdateMarketDataInput,
            SearchTransactionsInput
        )
        from mcp_persistence.server import portfolio_operations

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# ============================================================================
# ============================================================================
# Portfolio Tools using LangChain
# ============================================================================

@tool
def create_transaction_tool(input_data: CreateTransactionInput) -> str:
    """
    Create a new transaction in the portfolio.
    
    Args:
        input_data: Transaction details including ticker, type, quantity, etc.
        
    Returns:
        Transaction ID of the created transaction
    """
    response = portfolio_operations.create_transaction(input_data)
    if response.success and response.data:
        return json.dumps(response.data, indent=2)
    else:
        return response.message

@tool
def get_transaction_tool(transaction_id: str) -> str:
    """
    Get a transaction by its ID.
    
    Args:
        transaction_id: The ID of the transaction to retrieve
        
    Returns:
        JSON string of transaction details or error message
    """
    response = portfolio_operations.get_transaction(transaction_id)
    if response.success and response.data:
        return json.dumps(response.data, indent=2)
    else:
        return response.message

@tool
def update_transaction_tool(input_data: UpdateTransactionInput) -> str:
    """
    Update an existing transaction.
    
    Args:
        input_data: Transaction ID and fields to update
        
    Returns:
        Success message or error
    """
    response = portfolio_operations.update_transaction(input_data)
    return response.message

@tool
def delete_transaction_tool(transaction_id: str) -> str:
    """
    Delete a transaction by ID.
    
    Args:
        transaction_id: The ID of the transaction to delete
        
    Returns:
        Success message or error
    """
    response = portfolio_operations.delete_transaction(transaction_id)
    return response.message

@tool
def get_transactions_by_ticker_tool(ticker: str) -> str:
    """
    Get all transactions for a specific ticker.
    
    Args:
        ticker: Stock ticker symbol
        
    Returns:
        JSON string of transactions list
    """
    response = portfolio_operations.get_transactions_by_ticker(ticker)
    if response.success and response.data:
        return json.dumps(response.data, indent=2)
    else:
        return response.message

@tool
def get_all_positions_tool() -> str:
    """
    Get all current positions in the portfolio.
    
    Returns:
        JSON string of all positions
    """
    response = portfolio_operations.get_all_positions()
    if response.success and response.data:
        return json.dumps(response.data, indent=2)
    else:
        return response.message

@tool
def get_position_by_ticker_tool(ticker: str) -> str:
    """
    Get position details for a specific ticker.
    
    Args:
        ticker: Stock ticker symbol
        
    Returns:
        JSON string of position details
    """
    response = portfolio_operations.get_position_by_ticker(ticker)
    if response.success and response.data:
        return json.dumps(response.data, indent=2)
    else:
        return response.message

@tool
def update_market_data_tool(input_data: UpdateMarketDataInput) -> str:
    """
    Update market data for a position.
    
    Args:
        input_data: Ticker and current price information
        
    Returns:
        Success message or error
    """
    response = portfolio_operations.update_market_data(input_data)
    return response.message

@tool
def get_portfolio_summary_tool() -> str:
    """
    Get portfolio summary with key metrics.
    
    Returns:
        JSON string of portfolio summary
    """
    response = portfolio_operations.get_portfolio_summary()
    if response.success and response.data:
        return json.dumps(response.data, indent=2)
    else:
        return response.message

@tool
def get_ticker_analysis_tool(ticker: str) -> str:
    """
    Get comprehensive analysis for a specific ticker.
    
    Args:
        ticker: Stock ticker symbol
        
    Returns:
        JSON string of ticker analysis
    """
    response = portfolio_operations.get_ticker_analysis(ticker)
    if response.success and response.data:
        return json.dumps(response.data, indent=2)
    else:
        return response.message

@tool
def search_transactions_tool(input_data: SearchTransactionsInput) -> str:
    """
    Search transactions with multiple filters.
    
    Args:
        input_data: Search criteria including ticker, dates, type, etc.
        
    Returns:
        JSON string of matching transactions
    """
    response = portfolio_operations.search_transactions(input_data)
    if response.success and response.data:
        return json.dumps(response.data, indent=2)
    else:
        return response.message

@tool
def get_performance_metrics_tool() -> str:
    """
    Get portfolio performance metrics.
    
    Returns:
        JSON string of performance metrics
    """
    response = portfolio_operations.get_performance_metrics()
    if response.success and response.data:
        return json.dumps(response.data, indent=2)
    else:
        return response.message

@tool
def recalculate_position_tool(ticker: str) -> str:
    """
    Recalculate position for a specific ticker.
    
    Args:
        ticker: Stock ticker symbol
        
    Returns:
        Success message or error
    """
    response = portfolio_operations.recalculate_position(ticker)
    return response.message

@tool
def recalculate_all_positions_tool() -> str:
    """
    Recalculate all positions from transactions.
    
    Returns:
        Success message or error
    """
    response = portfolio_operations.recalculate_all_positions()
    return response.message

# ============================================================================
# Tool Collection
# ============================================================================

PORTFOLIO_TOOLS = [
    create_transaction_tool,
    get_transaction_tool,
    update_transaction_tool,
    delete_transaction_tool,
    get_transactions_by_ticker_tool,
    get_all_positions_tool,
    get_position_by_ticker_tool,
    update_market_data_tool,
    get_portfolio_summary_tool,
    get_ticker_analysis_tool,
    search_transactions_tool,
    get_performance_metrics_tool,
    recalculate_position_tool,
    recalculate_all_positions_tool
]

# ============================================================================
# Portfolio Tools Export
# ============================================================================

# All portfolio tools are available via the PORTFOLIO_TOOLS list above
# These tools can be used directly or imported by other modules (like HTTP server) 