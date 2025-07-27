"""
Portfolio Management REST API
FastAPI application that exposes all portfolio CRUD operations via REST endpoints
"""

from fastapi import FastAPI, HTTPException, Query
from fastapi.responses import JSONResponse
from typing import Optional, List
import uvicorn
import logging
import sys
import os

# Setup import paths
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))  # Add mcp_persistence to path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))  # Add root to path

# Import operations and models
from . import portfolio_operations
from .portfolio_operations import (
    CreateTransactionInput,
    UpdateTransactionInput,
    UpdateMarketDataInput,
    SearchTransactionsInput,
    OperationResponse
)

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Create FastAPI app
app = FastAPI(
    title="Portfolio Management API",
    description="REST API for managing portfolio transactions and positions",
    version="1.0.0"
)

# ============================================================================
# Transaction Endpoints
# ============================================================================

@app.post("/transactions", response_model=dict, summary="Create a new transaction")
async def create_transaction(transaction: CreateTransactionInput):
    """Create a new transaction in the portfolio"""
    response = portfolio_operations.create_transaction(transaction)
    
    if response.success:
        return {
            "success": True,
            "message": response.message,
            "data": response.data
        }
    else:
        raise HTTPException(status_code=400, detail=response.message)

@app.get("/transactions/{transaction_id}", response_model=dict, summary="Get transaction by ID")
async def get_transaction(transaction_id: str):
    """Get a transaction by its ID"""
    response = portfolio_operations.get_transaction(transaction_id)
    
    if response.success:
        return {
            "success": True,
            "message": response.message,
            "data": response.data
        }
    else:
        raise HTTPException(status_code=404, detail=response.message)

@app.put("/transactions", response_model=dict, summary="Update a transaction")
async def update_transaction(transaction: UpdateTransactionInput):
    """Update an existing transaction"""
    response = portfolio_operations.update_transaction(transaction)
    
    if response.success:
        return {
            "success": True,
            "message": response.message
        }
    else:
        raise HTTPException(status_code=400, detail=response.message)

@app.delete("/transactions/{transaction_id}", response_model=dict, summary="Delete a transaction")
async def delete_transaction(transaction_id: str):
    """Delete a transaction by ID"""
    response = portfolio_operations.delete_transaction(transaction_id)
    
    if response.success:
        return {
            "success": True,
            "message": response.message
        }
    else:
        raise HTTPException(status_code=404, detail=response.message)

@app.get("/transactions/ticker/{ticker}", response_model=dict, summary="Get transactions by ticker")
async def get_transactions_by_ticker(ticker: str):
    """Get all transactions for a specific ticker"""
    response = portfolio_operations.get_transactions_by_ticker(ticker)
    
    if response.success:
        return {
            "success": True,
            "message": response.message,
            "data": response.data
        }
    else:
        raise HTTPException(status_code=400, detail=response.message)

@app.post("/transactions/search", response_model=dict, summary="Search transactions")
async def search_transactions(search_criteria: SearchTransactionsInput):
    """Search transactions with multiple filters"""
    response = portfolio_operations.search_transactions(search_criteria)
    
    if response.success:
        return {
            "success": True,
            "message": response.message,
            "data": response.data
        }
    else:
        raise HTTPException(status_code=400, detail=response.message)

# ============================================================================
# Position Endpoints
# ============================================================================

@app.get("/positions", response_model=dict, summary="Get all positions")
async def get_all_positions():
    """Get all current positions in the portfolio"""
    response = portfolio_operations.get_all_positions()
    
    if response.success:
        return {
            "success": True,
            "message": response.message,
            "data": response.data
        }
    else:
        raise HTTPException(status_code=400, detail=response.message)

@app.get("/positions/{ticker}", response_model=dict, summary="Get position by ticker")
async def get_position_by_ticker(ticker: str):
    """Get position details for a specific ticker"""
    response = portfolio_operations.get_position_by_ticker(ticker)
    
    if response.success:
        return {
            "success": True,
            "message": response.message,
            "data": response.data
        }
    else:
        raise HTTPException(status_code=404, detail=response.message)

@app.put("/positions/market-data", response_model=dict, summary="Update market data")
async def update_market_data(market_data: UpdateMarketDataInput):
    """Update market data for a position"""
    response = portfolio_operations.update_market_data(market_data)
    
    if response.success:
        return {
            "success": True,
            "message": response.message
        }
    else:
        raise HTTPException(status_code=400, detail=response.message)

@app.post("/positions/{ticker}/recalculate", response_model=dict, summary="Recalculate position")
async def recalculate_position(ticker: str):
    """Recalculate position for a specific ticker"""
    response = portfolio_operations.recalculate_position(ticker)
    
    if response.success:
        return {
            "success": True,
            "message": response.message
        }
    else:
        raise HTTPException(status_code=400, detail=response.message)

@app.post("/positions/recalculate-all", response_model=dict, summary="Recalculate all positions")
async def recalculate_all_positions():
    """Recalculate all positions from transactions"""
    response = portfolio_operations.recalculate_all_positions()
    
    if response.success:
        return {
            "success": True,
            "message": response.message
        }
    else:
        raise HTTPException(status_code=400, detail=response.message)

# ============================================================================
# Portfolio Analysis Endpoints
# ============================================================================

@app.get("/portfolio/summary", response_model=dict, summary="Get portfolio summary")
async def get_portfolio_summary():
    """Get portfolio summary with key metrics"""
    response = portfolio_operations.get_portfolio_summary()
    
    if response.success:
        return {
            "success": True,
            "message": response.message,
            "data": response.data
        }
    else:
        raise HTTPException(status_code=400, detail=response.message)

@app.get("/portfolio/performance", response_model=dict, summary="Get performance metrics")
async def get_performance_metrics():
    """Get portfolio performance metrics"""
    response = portfolio_operations.get_performance_metrics()
    
    if response.success:
        return {
            "success": True,
            "message": response.message,
            "data": response.data
        }
    else:
        raise HTTPException(status_code=400, detail=response.message)

@app.get("/analysis/{ticker}", response_model=dict, summary="Get ticker analysis")
async def get_ticker_analysis(ticker: str):
    """Get comprehensive analysis for a specific ticker"""
    response = portfolio_operations.get_ticker_analysis(ticker)
    
    if response.success:
        return {
            "success": True,
            "message": response.message,
            "data": response.data
        }
    else:
        raise HTTPException(status_code=400, detail=response.message)

# ============================================================================
# Health Check and Documentation
# ============================================================================

@app.get("/health", summary="Health check")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy", "service": "Portfolio Management API"}

@app.get("/", summary="API Info")
async def root():
    """API information and welcome message"""
    return {
        "message": "Welcome to Portfolio Management API",
        "version": "1.0.0",
        "docs": "/docs",
        "health": "/health"
    }

# ============================================================================
# Exception Handlers
# ============================================================================

@app.exception_handler(Exception)
async def general_exception_handler(request, exc):
    """Handle general exceptions"""
    logger.error(f"Unhandled exception: {exc}")
    return JSONResponse(
        status_code=500,
        content={"error": "Internal server error", "detail": str(exc)}
    )

# ============================================================================
# Main Function for Running the Server
# ============================================================================

def run_server(host: str = "0.0.0.0", port: int = 8000):
    """Run the FastAPI server"""
    uvicorn.run(app, host=host, port=port)

if __name__ == "__main__":
    run_server() 