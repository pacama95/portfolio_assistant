"""
CRUD operations for Portfolio Management System
Provides all database operations for transactions and positions
"""

import uuid
from typing import List, Dict, Any, Optional
from datetime import datetime, date
from decimal import Decimal

from ..database import get_db_manager, TransactionCreate, TransactionUpdate, PositionUpdate
import logging

logger = logging.getLogger(__name__)


class TransactionCRUD:
    """CRUD operations for transactions table"""
    
    def __init__(self):
        self.db = get_db_manager()
    
    def create_transaction(self, transaction: TransactionCreate) -> str:
        """Create a new transaction"""
        query = """
        INSERT INTO transactions (
            ticker, transaction_type, quantity, cost_per_share, currency,
            transaction_date, commission, commission_currency, drip_confirmed,
            is_fractional, fractional_multiplier, notes
        ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        RETURNING id
        """
        
        params = (
            transaction.ticker.upper(),
            transaction.transaction_type.upper(),
            transaction.quantity,
            transaction.cost_per_share,
            transaction.currency.upper(),
            transaction.transaction_date,
            transaction.commission,
            transaction.commission_currency.upper() if transaction.commission_currency else None,
            transaction.drip_confirmed,
            transaction.is_fractional,
            transaction.fractional_multiplier,
            transaction.notes
        )
        
        try:
            result = self.db.execute_query(query, params)
            transaction_id = result[0]['id']
            actual_shares = transaction.quantity * transaction.fractional_multiplier
            logger.info(f"Created transaction {transaction_id} for {transaction.ticker} " +
                       f"(quantity: {transaction.quantity}, actual shares: {actual_shares})")
            return str(transaction_id)
        except Exception as e:
            logger.error(f"Failed to create transaction: {e}")
            raise
    
    def get_transaction_by_id(self, transaction_id: str) -> Optional[Dict[str, Any]]:
        """Get a transaction by ID"""
        query = "SELECT * FROM transactions WHERE id = %s"
        try:
            results = self.db.execute_query(query, (transaction_id,))
            return results[0] if results else None
        except Exception as e:
            logger.error(f"Failed to get transaction {transaction_id}: {e}")
            raise
    
    def get_transactions_by_ticker(self, ticker: str) -> List[Dict[str, Any]]:
        """Get all transactions for a specific ticker"""
        query = """
        SELECT * FROM transactions 
        WHERE ticker = %s 
        ORDER BY transaction_date DESC, created_at DESC
        """
        try:
            return self.db.execute_query(query, (ticker.upper(),))
        except Exception as e:
            logger.error(f"Failed to get transactions for {ticker}: {e}")
            raise
    
    def get_all_transactions(self, limit: Optional[int] = None, offset: int = 0) -> List[Dict[str, Any]]:
        """Get all transactions with optional pagination"""
        query = """
        SELECT * FROM transactions 
        ORDER BY transaction_date DESC, created_at DESC
        """
        if limit:
            query += f" LIMIT {limit} OFFSET {offset}"
        
        try:
            return self.db.execute_query(query)
        except Exception as e:
            logger.error(f"Failed to get all transactions: {e}")
            raise
    
    def update_transaction(self, transaction_id: str, updates: TransactionUpdate) -> bool:
        """Update a transaction"""
        # Build dynamic update query
        update_fields = []
        params = []
        
        for field, value in updates.dict(exclude_unset=True).items():
            if value is not None:
                if field in ['ticker', 'transaction_type', 'currency', 'commission_currency']:
                    value = value.upper()
                update_fields.append(f"{field} = %s")
                params.append(value)
        
        if not update_fields:
            return False
        
        query = f"""
        UPDATE transactions 
        SET {', '.join(update_fields)}, updated_at = CURRENT_TIMESTAMP
        WHERE id = %s
        """
        params.append(transaction_id)
        
        try:
            rows_affected = self.db.execute_command(query, tuple(params))
            logger.info(f"Updated transaction {transaction_id}")
            return rows_affected > 0
        except Exception as e:
            logger.error(f"Failed to update transaction {transaction_id}: {e}")
            raise
    
    def delete_transaction(self, transaction_id: str) -> bool:
        """Delete a transaction"""
        query = "DELETE FROM transactions WHERE id = %s"
        try:
            rows_affected = self.db.execute_command(query, (transaction_id,))
            logger.info(f"Deleted transaction {transaction_id}")
            return rows_affected > 0
        except Exception as e:
            logger.error(f"Failed to delete transaction {transaction_id}: {e}")
            raise
    
    def bulk_create_transactions(self, transactions: List[TransactionCreate]) -> List[str]:
        """Create multiple transactions in a batch"""
        query = """
        INSERT INTO transactions (
            ticker, transaction_type, quantity, cost_per_share, currency,
            transaction_date, commission, commission_currency, drip_confirmed,
            is_fractional, fractional_multiplier, notes
        ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        RETURNING id
        """
        
        params_list = []
        for transaction in transactions:
            params = (
                transaction.ticker.upper(),
                transaction.transaction_type.upper(),
                transaction.quantity,
                transaction.cost_per_share,
                transaction.currency.upper(),
                transaction.transaction_date,
                transaction.commission,
                transaction.commission_currency.upper() if transaction.commission_currency else None,
                transaction.drip_confirmed,
                transaction.is_fractional,
                transaction.fractional_multiplier,
                transaction.notes
            )
            params_list.append(params)
        
        try:
            # Use individual inserts to get IDs back
            transaction_ids = []
            fractional_count = 0
            for params in params_list:
                result = self.db.execute_query(query, params)
                transaction_ids.append(str(result[0]['id']))
                if params[9]:  # is_fractional is at index 9
                    fractional_count += 1
            
            logger.info(f"Created {len(transaction_ids)} transactions ({fractional_count} fractional)")
            return transaction_ids
        except Exception as e:
            logger.error(f"Failed to bulk create transactions: {e}")
            raise


class PositionCRUD:
    """CRUD operations for positions table"""
    
    def __init__(self):
        self.db = get_db_manager()
    
    def get_position_by_ticker(self, ticker: str) -> Optional[Dict[str, Any]]:
        """Get a position by ticker"""
        query = "SELECT * FROM positions WHERE ticker = %s"
        try:
            results = self.db.execute_query(query, (ticker.upper(),))
            return results[0] if results else None
        except Exception as e:
            logger.error(f"Failed to get position for {ticker}: {e}")
            raise
    
    def get_all_positions(self) -> List[Dict[str, Any]]:
        """Get all current positions"""
        query = """
        SELECT * FROM positions 
        WHERE current_quantity > 0 
        ORDER BY ticker
        """
        try:
            return self.db.execute_query(query)
        except Exception as e:
            logger.error(f"Failed to get all positions: {e}")
            raise
    
    def get_position_details(self) -> List[Dict[str, Any]]:
        """Get detailed position information with calculations"""
        query = "SELECT * FROM position_details ORDER BY ticker"
        try:
            return self.db.execute_query(query)
        except Exception as e:
            logger.error(f"Failed to get position details: {e}")
            raise
    
    def get_portfolio_summary(self) -> Dict[str, Any]:
        """Get portfolio summary statistics"""
        query = "SELECT * FROM portfolio_summary"
        try:
            results = self.db.execute_query(query)
            return results[0] if results else {}
        except Exception as e:
            logger.error(f"Failed to get portfolio summary: {e}")
            raise
    
    def update_position_market_data(self, update: PositionUpdate) -> bool:
        """Update position with current market data"""
        # Calculate derived values
        current_market_value = None
        unrealized_gain_loss = None
        
        # Get current position data to calculate values
        position = self.get_position_by_ticker(update.ticker)
        if position:
            current_quantity = Decimal(str(position['current_quantity']))
            total_cost_basis = Decimal(str(position['total_cost_basis']))
            
            current_market_value = current_quantity * update.current_price
            unrealized_gain_loss = current_market_value - total_cost_basis
        
        query = """
        UPDATE positions 
        SET current_price = %s,
            current_market_value = %s,
            unrealized_gain_loss = %s,
            last_price_update = %s,
            updated_at = CURRENT_TIMESTAMP
        WHERE ticker = %s
        """
        
        params = (
            update.current_price,
            current_market_value,
            unrealized_gain_loss,
            update.last_price_update or datetime.now(),
            update.ticker.upper()
        )
        
        try:
            rows_affected = self.db.execute_command(query, params)
            logger.info(f"Updated market data for {update.ticker}")
            return rows_affected > 0
        except Exception as e:
            logger.error(f"Failed to update market data for {update.ticker}: {e}")
            raise
    
    def recalculate_position(self, ticker: str) -> bool:
        """Manually trigger position recalculation for a ticker"""
        query = "SELECT recalculate_position(%s)"
        try:
            self.db.execute_query(query, (ticker.upper(),))
            logger.info(f"Recalculated position for {ticker}")
            return True
        except Exception as e:
            logger.error(f"Failed to recalculate position for {ticker}: {e}")
            raise
    
    def recalculate_all_positions(self) -> bool:
        """Recalculate all positions from transactions"""
        try:
            # Get all unique tickers from transactions
            tickers_query = "SELECT DISTINCT ticker FROM transactions"
            tickers = self.db.execute_query(tickers_query)
            
            for row in tickers:
                self.recalculate_position(row['ticker'])
            
            logger.info(f"Recalculated {len(tickers)} positions")
            return True
        except Exception as e:
            logger.error(f"Failed to recalculate all positions: {e}")
            raise


class PortfolioCRUD:
    """Combined CRUD operations and utility functions"""
    
    def __init__(self):
        self.transactions = TransactionCRUD()
        self.positions = PositionCRUD()
        self.db = get_db_manager()
    
    def get_ticker_analysis(self, ticker: str) -> Dict[str, Any]:
        """Get comprehensive analysis for a specific ticker"""
        try:
            # Get position
            position = self.positions.get_position_by_ticker(ticker)
            
            # Get transactions
            transactions = self.transactions.get_transactions_by_ticker(ticker)
            
            # Calculate additional metrics
            total_bought = sum(t['quantity'] for t in transactions if t['transaction_type'] == 'BUY')
            total_sold = sum(t['quantity'] for t in transactions if t['transaction_type'] == 'SELL')
            total_transactions = len(transactions)
            
            return {
                'ticker': ticker.upper(),
                'position': position,
                'transactions': transactions,
                'metrics': {
                    'total_bought': float(total_bought),
                    'total_sold': float(total_sold),
                    'net_quantity': float(total_bought - total_sold),
                    'total_transactions': total_transactions
                }
            }
        except Exception as e:
            logger.error(f"Failed to get ticker analysis for {ticker}: {e}")
            raise
    
    def get_currency_breakdown(self) -> Dict[str, Any]:
        """Get portfolio breakdown by currency"""
        query = """
        SELECT 
            primary_currency,
            COUNT(*) as positions_count,
            SUM(current_quantity * current_price) as total_market_value,
            SUM(total_cost_basis) as total_cost_basis,
            SUM(unrealized_gain_loss) as total_unrealized_pnl
        FROM positions 
        WHERE current_quantity > 0
        GROUP BY primary_currency
        ORDER BY total_market_value DESC
        """
        try:
            return self.db.execute_query(query)
        except Exception as e:
            logger.error(f"Failed to get currency breakdown: {e}")
            raise
    
    def get_performance_metrics(self) -> Dict[str, Any]:
        """Get portfolio performance metrics"""
        query = """
        SELECT 
            COUNT(*) as total_positions,
            COUNT(CASE WHEN unrealized_gain_loss > 0 THEN 1 END) as winning_positions,
            COUNT(CASE WHEN unrealized_gain_loss < 0 THEN 1 END) as losing_positions,
            AVG(unrealized_gain_loss) as avg_unrealized_pnl,
            MAX(unrealized_gain_loss) as best_performer,
            MIN(unrealized_gain_loss) as worst_performer,
            SUM(current_quantity * current_price) as total_market_value,
            SUM(total_cost_basis) as total_invested
        FROM positions 
        WHERE current_quantity > 0
        """
        try:
            result = self.db.execute_query(query)[0]
            
            # Calculate additional metrics
            if result['total_invested'] and float(result['total_invested']) > 0:
                result['total_return_percentage'] = (
                    (float(result['total_market_value']) - float(result['total_invested'])) / 
                    float(result['total_invested']) * 100
                )
            else:
                result['total_return_percentage'] = 0
            
            if result['total_positions'] and int(result['total_positions']) > 0:
                result['win_rate'] = (
                    int(result['winning_positions']) / int(result['total_positions']) * 100
                )
            else:
                result['win_rate'] = 0
            
            return result
        except Exception as e:
            logger.error(f"Failed to get performance metrics: {e}")
            raise
    
    def search_transactions(self, 
                          ticker: Optional[str] = None,
                          start_date: Optional[date] = None,
                          end_date: Optional[date] = None,
                          transaction_type: Optional[str] = None,
                          min_quantity: Optional[Decimal] = None,
                          max_quantity: Optional[Decimal] = None) -> List[Dict[str, Any]]:
        """Search transactions with multiple filters"""
        conditions = []
        params = []
        
        if ticker:
            conditions.append("ticker = %s")
            params.append(ticker.upper())
        
        if start_date:
            conditions.append("transaction_date >= %s")
            params.append(start_date)
        
        if end_date:
            conditions.append("transaction_date <= %s")
            params.append(end_date)
        
        if transaction_type:
            conditions.append("transaction_type = %s")
            params.append(transaction_type.upper())
        
        if min_quantity:
            conditions.append("ABS(quantity) >= %s")
            params.append(min_quantity)
        
        if max_quantity:
            conditions.append("ABS(quantity) <= %s")
            params.append(max_quantity)
        
        where_clause = " AND ".join(conditions) if conditions else "1=1"
        
        query = f"""
        SELECT * FROM transactions 
        WHERE {where_clause}
        ORDER BY transaction_date DESC, created_at DESC
        """
        
        try:
            return self.db.execute_query(query, tuple(params))
        except Exception as e:
            logger.error(f"Failed to search transactions: {e}")
            raise


# Create global instances
transaction_crud = TransactionCRUD()
position_crud = PositionCRUD()
portfolio_crud = PortfolioCRUD()


# Convenience functions
def create_transaction(transaction_data: dict) -> str:
    """Convenience function to create a transaction"""
    transaction = TransactionCreate(**transaction_data)
    return transaction_crud.create_transaction(transaction)


def get_all_positions() -> List[Dict[str, Any]]:
    """Convenience function to get all positions"""
    return position_crud.get_all_positions()


def get_portfolio_summary() -> Dict[str, Any]:
    """Convenience function to get portfolio summary"""
    return position_crud.get_portfolio_summary()


def update_market_price(ticker: str, price: Decimal) -> bool:
    """Convenience function to update market price"""
    update = PositionUpdate(
        ticker=ticker,
        current_price=price,
        last_price_update=datetime.now()
    )
    return position_crud.update_position_market_data(update)


if __name__ == "__main__":
    # Test CRUD operations
    print("Testing CRUD operations...")
    
    try:
        # Test getting positions
        positions = get_all_positions()
        print(f"Current positions: {len(positions)}")
        
        # Test portfolio summary
        summary = get_portfolio_summary()
        print(f"Portfolio summary: {summary}")
        
    except Exception as e:
        print(f"Error testing CRUD operations: {e}") 