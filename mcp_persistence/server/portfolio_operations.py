"""
Portfolio Management Operations
Contains all portfolio CRUD operations and input validation models
"""

import json
import logging
from typing import Optional, Dict, Any, List
from datetime import datetime, date
from decimal import Decimal

# Pydantic
from pydantic import BaseModel, Field

# Setup import paths
import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))  # Add mcp_persistence to path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))  # Add root to path

# Local imports
from persistence import (
    transaction_crud, position_crud, portfolio_crud,
    TransactionCreate, TransactionUpdate, PositionUpdate
)

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# ============================================================================
# Pydantic Models for Input Validation
# ============================================================================

class CreateTransactionInput(BaseModel):
    """Input model for creating a transaction"""
    ticker: str = Field(..., description="Stock ticker symbol")
    transaction_type: str = Field(..., description="Transaction type (BUY, SELL)")
    quantity: float = Field(..., description="Number of shares")
    cost_per_share: float = Field(..., description="Price per share")
    currency: str = Field(default="USD", description="Currency code")
    transaction_date: str = Field(..., description="Transaction date (YYYY-MM-DD)")
    commission: Optional[float] = Field(None, description="Commission paid")
    commission_currency: Optional[str] = Field(None, description="Commission currency")
    drip_confirmed: bool = Field(default=False, description="DRIP confirmation status")
    notes: Optional[str] = Field(None, description="Additional notes")

class UpdateTransactionInput(BaseModel):
    """Input model for updating a transaction"""
    transaction_id: str = Field(..., description="Transaction ID to update")
    ticker: Optional[str] = Field(None, description="Stock ticker symbol")
    transaction_type: Optional[str] = Field(None, description="Transaction type")
    quantity: Optional[float] = Field(None, description="Number of shares")
    cost_per_share: Optional[float] = Field(None, description="Price per share")
    currency: Optional[str] = Field(None, description="Currency code")
    transaction_date: Optional[str] = Field(None, description="Transaction date")
    commission: Optional[float] = Field(None, description="Commission paid")
    commission_currency: Optional[str] = Field(None, description="Commission currency")
    drip_confirmed: Optional[bool] = Field(None, description="DRIP confirmation")
    notes: Optional[str] = Field(None, description="Additional notes")

class UpdateMarketDataInput(BaseModel):
    """Input model for updating market data"""
    ticker: str = Field(..., description="Stock ticker symbol")
    current_price: float = Field(..., description="Current market price")
    last_price_update: Optional[str] = Field(None, description="Price update timestamp")

class SearchTransactionsInput(BaseModel):
    """Input model for searching transactions"""
    ticker: Optional[str] = Field(None, description="Filter by ticker")
    start_date: Optional[str] = Field(None, description="Start date (YYYY-MM-DD)")
    end_date: Optional[str] = Field(None, description="End date (YYYY-MM-DD)")
    transaction_type: Optional[str] = Field(None, description="Transaction type filter")
    min_quantity: Optional[float] = Field(None, description="Minimum quantity")
    max_quantity: Optional[float] = Field(None, description="Maximum quantity")

# ============================================================================
# Response Models
# ============================================================================

class OperationResponse(BaseModel):
    """Standard response model for operations"""
    success: bool
    message: str
    data: Optional[Dict[Any, Any]] = None

# ============================================================================
# Utility Functions
# ============================================================================

def convert_for_json(obj):
    """Convert objects for JSON serialization"""
    if isinstance(obj, dict):
        return {k: convert_for_json(v) for k, v in obj.items()}
    elif isinstance(obj, list):
        return [convert_for_json(item) for item in obj]
    elif isinstance(obj, Decimal):
        return float(obj)
    elif isinstance(obj, (date, datetime)):
        return obj.isoformat()
    else:
        return obj

# ============================================================================
# Portfolio CRUD Operations
# ============================================================================

def create_transaction(input_data: CreateTransactionInput) -> OperationResponse:
    """
    Create a new transaction in the portfolio.
    
    Args:
        input_data: Transaction details including ticker, type, quantity, etc.
        
    Returns:
        OperationResponse with transaction ID or error
    """
    try:
        # Convert date string to date object
        transaction_date = datetime.strptime(input_data.transaction_date, "%Y-%m-%d").date()
        
        # Create transaction object
        transaction = TransactionCreate(
            ticker=input_data.ticker,
            transaction_type=input_data.transaction_type,
            quantity=Decimal(str(input_data.quantity)),
            cost_per_share=Decimal(str(input_data.cost_per_share)),
            currency=input_data.currency,
            transaction_date=transaction_date,
            commission=Decimal(str(input_data.commission)) if input_data.commission else None,
            commission_currency=input_data.commission_currency,
            drip_confirmed=input_data.drip_confirmed,
            notes=input_data.notes
        )
        
        transaction_id = transaction_crud.create_transaction(transaction)
        return OperationResponse(
            success=True,
            message=f"Successfully created transaction {transaction_id}",
            data={"transaction_id": transaction_id}
        )
        
    except Exception as e:
        logger.error(f"Error creating transaction: {e}")
        return OperationResponse(
            success=False,
            message=f"Error creating transaction: {str(e)}"
        )

def get_transaction(transaction_id: str) -> OperationResponse:
    """
    Get a transaction by its ID.
    
    Args:
        transaction_id: The ID of the transaction to retrieve
        
    Returns:
        OperationResponse with transaction details or error
    """
    try:
        transaction = transaction_crud.get_transaction_by_id(transaction_id)
        if transaction:
            converted_transaction = convert_for_json(transaction)
            return OperationResponse(
                success=True,
                message="Transaction retrieved successfully",
                data=converted_transaction
            )
        else:
            return OperationResponse(
                success=False,
                message=f"Transaction {transaction_id} not found"
            )
    except Exception as e:
        logger.error(f"Error getting transaction: {e}")
        return OperationResponse(
            success=False,
            message=f"Error getting transaction: {str(e)}"
        )

def update_transaction(input_data: UpdateTransactionInput) -> OperationResponse:
    """
    Update an existing transaction.
    
    Args:
        input_data: Transaction ID and fields to update
        
    Returns:
        OperationResponse with success status
    """
    try:
        # Prepare update data
        update_dict = {}
        for field, value in input_data.dict(exclude_unset=True).items():
            if field != "transaction_id" and value is not None:
                if field == "transaction_date":
                    update_dict[field] = datetime.strptime(value, "%Y-%m-%d").date()
                elif field in ["quantity", "cost_per_share", "commission"]:
                    update_dict[field] = Decimal(str(value))
                else:
                    update_dict[field] = value
        
        updates = TransactionUpdate(**update_dict)
        success = transaction_crud.update_transaction(input_data.transaction_id, updates)
        
        if success:
            return OperationResponse(
                success=True,
                message=f"Successfully updated transaction {input_data.transaction_id}"
            )
        else:
            return OperationResponse(
                success=False,
                message=f"Transaction {input_data.transaction_id} not found or no changes made"
            )
            
    except Exception as e:
        logger.error(f"Error updating transaction: {e}")
        return OperationResponse(
            success=False,
            message=f"Error updating transaction: {str(e)}"
        )

def delete_transaction(transaction_id: str) -> OperationResponse:
    """
    Delete a transaction by ID.
    
    Args:
        transaction_id: The ID of the transaction to delete
        
    Returns:
        OperationResponse with success status
    """
    try:
        success = transaction_crud.delete_transaction(transaction_id)
        if success:
            return OperationResponse(
                success=True,
                message=f"Successfully deleted transaction {transaction_id}"
            )
        else:
            return OperationResponse(
                success=False,
                message=f"Transaction {transaction_id} not found"
            )
    except Exception as e:
        logger.error(f"Error deleting transaction: {e}")
        return OperationResponse(
            success=False,
            message=f"Error deleting transaction: {str(e)}"
        )

def get_transactions_by_ticker(ticker: str) -> OperationResponse:
    """
    Get all transactions for a specific ticker.
    
    Args:
        ticker: Stock ticker symbol
        
    Returns:
        OperationResponse with transactions list
    """
    try:
        transactions = transaction_crud.get_transactions_by_ticker(ticker)
        converted_transactions = convert_for_json(transactions)
        
        return OperationResponse(
            success=True,
            message=f"Retrieved {len(transactions)} transactions for {ticker}",
            data=converted_transactions
        )
    except Exception as e:
        logger.error(f"Error getting transactions for {ticker}: {e}")
        return OperationResponse(
            success=False,
            message=f"Error getting transactions: {str(e)}"
        )

def get_all_positions() -> OperationResponse:
    """
    Get all current positions in the portfolio.
    
    Returns:
        OperationResponse with all positions
    """
    try:
        positions = position_crud.get_all_positions()
        converted_positions = convert_for_json(positions)
        
        return OperationResponse(
            success=True,
            message=f"Retrieved {len(positions)} positions",
            data=converted_positions
        )
    except Exception as e:
        logger.error(f"Error getting positions: {e}")
        return OperationResponse(
            success=False,
            message=f"Error getting positions: {str(e)}"
        )

def get_position_by_ticker(ticker: str) -> OperationResponse:
    """
    Get position details for a specific ticker.
    
    Args:
        ticker: Stock ticker symbol
        
    Returns:
        OperationResponse with position details
    """
    try:
        position = position_crud.get_position_by_ticker(ticker)
        if position:
            converted_position = convert_for_json(position)
            return OperationResponse(
                success=True,
                message=f"Position retrieved for {ticker}",
                data=converted_position
            )
        else:
            return OperationResponse(
                success=False,
                message=f"No position found for ticker {ticker}"
            )
    except Exception as e:
        logger.error(f"Error getting position for {ticker}: {e}")
        return OperationResponse(
            success=False,
            message=f"Error getting position: {str(e)}"
        )

def update_market_data(input_data: UpdateMarketDataInput) -> OperationResponse:
    """
    Update market data for a position.
    
    Args:
        input_data: Ticker and current price information
        
    Returns:
        OperationResponse with success status
    """
    try:
        last_update = None
        if input_data.last_price_update:
            last_update = datetime.fromisoformat(input_data.last_price_update)
        
        update = PositionUpdate(
            ticker=input_data.ticker,
            current_price=Decimal(str(input_data.current_price)),
            last_price_update=last_update
        )
        
        success = position_crud.update_position_market_data(update)
        if success:
            return OperationResponse(
                success=True,
                message=f"Successfully updated market data for {input_data.ticker}"
            )
        else:
            return OperationResponse(
                success=False,
                message=f"Position for {input_data.ticker} not found"
            )
            
    except Exception as e:
        logger.error(f"Error updating market data: {e}")
        return OperationResponse(
            success=False,
            message=f"Error updating market data: {str(e)}"
        )

def get_portfolio_summary() -> OperationResponse:
    """
    Get portfolio summary with key metrics.
    
    Returns:
        OperationResponse with portfolio summary
    """
    try:
        summary = position_crud.get_portfolio_summary()
        converted_summary = convert_for_json(summary)
        
        return OperationResponse(
            success=True,
            message="Portfolio summary retrieved successfully",
            data=converted_summary
        )
    except Exception as e:
        logger.error(f"Error getting portfolio summary: {e}")
        return OperationResponse(
            success=False,
            message=f"Error getting portfolio summary: {str(e)}"
        )

def get_ticker_analysis(ticker: str) -> OperationResponse:
    """
    Get comprehensive analysis for a specific ticker.
    
    Args:
        ticker: Stock ticker symbol
        
    Returns:
        OperationResponse with ticker analysis
    """
    try:
        analysis = portfolio_crud.get_ticker_analysis(ticker)
        converted_analysis = convert_for_json(analysis)
        
        return OperationResponse(
            success=True,
            message=f"Analysis retrieved for {ticker}",
            data=converted_analysis
        )
        
    except Exception as e:
        logger.error(f"Error getting ticker analysis: {e}")
        return OperationResponse(
            success=False,
            message=f"Error getting ticker analysis: {str(e)}"
        )

def search_transactions(input_data: SearchTransactionsInput) -> OperationResponse:
    """
    Search transactions with multiple filters.
    
    Args:
        input_data: Search criteria including ticker, dates, type, etc.
        
    Returns:
        OperationResponse with matching transactions
    """
    try:
        # Convert string dates to date objects
        start_date = None
        end_date = None
        if input_data.start_date:
            start_date = datetime.strptime(input_data.start_date, "%Y-%m-%d").date()
        if input_data.end_date:
            end_date = datetime.strptime(input_data.end_date, "%Y-%m-%d").date()
        
        # Convert quantity filters
        min_quantity = Decimal(str(input_data.min_quantity)) if input_data.min_quantity else None
        max_quantity = Decimal(str(input_data.max_quantity)) if input_data.max_quantity else None
        
        transactions = portfolio_crud.search_transactions(
            ticker=input_data.ticker,
            start_date=start_date,
            end_date=end_date,
            transaction_type=input_data.transaction_type,
            min_quantity=min_quantity,
            max_quantity=max_quantity
        )
        
        converted_transactions = convert_for_json(transactions)
        
        return OperationResponse(
            success=True,
            message=f"Found {len(transactions)} matching transactions",
            data=converted_transactions
        )
        
    except Exception as e:
        logger.error(f"Error searching transactions: {e}")
        return OperationResponse(
            success=False,
            message=f"Error searching transactions: {str(e)}"
        )

def get_performance_metrics() -> OperationResponse:
    """
    Get portfolio performance metrics.
    
    Returns:
        OperationResponse with performance metrics
    """
    try:
        metrics = portfolio_crud.get_performance_metrics()
        converted_metrics = convert_for_json(metrics)
        
        return OperationResponse(
            success=True,
            message="Performance metrics retrieved successfully",
            data=converted_metrics
        )
    except Exception as e:
        logger.error(f"Error getting performance metrics: {e}")
        return OperationResponse(
            success=False,
            message=f"Error getting performance metrics: {str(e)}"
        )

def recalculate_position(ticker: str) -> OperationResponse:
    """
    Recalculate position for a specific ticker.
    
    Args:
        ticker: Stock ticker symbol
        
    Returns:
        OperationResponse with success status
    """
    try:
        success = position_crud.recalculate_position(ticker)
        if success:
            return OperationResponse(
                success=True,
                message=f"Successfully recalculated position for {ticker}"
            )
        else:
            return OperationResponse(
                success=False,
                message=f"Failed to recalculate position for {ticker}"
            )
    except Exception as e:
        logger.error(f"Error recalculating position: {e}")
        return OperationResponse(
            success=False,
            message=f"Error recalculating position: {str(e)}"
        )

def recalculate_all_positions() -> OperationResponse:
    """
    Recalculate all positions from transactions.
    
    Returns:
        OperationResponse with success status
    """
    try:
        success = position_crud.recalculate_all_positions()
        if success:
            return OperationResponse(
                success=True,
                message="Successfully recalculated all positions"
            )
        else:
            return OperationResponse(
                success=False,
                message="Failed to recalculate positions"
            )
    except Exception as e:
        logger.error(f"Error recalculating all positions: {e}")
        return OperationResponse(
            success=False,
            message=f"Error recalculating all positions: {str(e)}"
        ) 