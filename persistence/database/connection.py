"""
Database utilities for Portfolio Management System
Provides connection management and base configurations for PostgreSQL database
"""

import os
import logging
from typing import Optional, Dict, Any, List
from contextlib import contextmanager
from datetime import datetime, date
from decimal import Decimal

import psycopg2
from psycopg2.extras import RealDictCursor
from psycopg2.pool import SimpleConnectionPool
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker, Session
from sqlalchemy.pool import QueuePool

from pydantic import BaseModel, Field
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class DatabaseConfig:
    """Database configuration class"""
    
    def __init__(self):
        self.host = os.getenv("DB_HOST", "localhost")
        self.port = int(os.getenv("DB_PORT", "5432"))
        self.database = os.getenv("DB_NAME", "portfolio_db")
        self.user = os.getenv("DB_USER", "portfolio_user")
        self.password = os.getenv("DB_PASSWORD", "portfolio_password")
        self.pool_size = int(os.getenv("DB_POOL_SIZE", "5"))
        self.max_overflow = int(os.getenv("DB_MAX_OVERFLOW", "10"))
    
    @property
    def connection_string(self) -> str:
        """Get SQLAlchemy connection string"""
        return f"postgresql://{self.user}:{self.password}@{self.host}:{self.port}/{self.database}"
    
    @property
    def psycopg2_params(self) -> Dict[str, Any]:
        """Get psycopg2 connection parameters"""
        return {
            "host": self.host,
            "port": self.port,
            "database": self.database,
            "user": self.user,
            "password": self.password
        }


class DatabaseManager:
    """Manages database connections and provides utility methods"""
    
    def __init__(self, config: Optional[DatabaseConfig] = None):
        self.config = config or DatabaseConfig()
        self._engine = None
        self._session_factory = None
        self._connection_pool = None
    
    @property
    def engine(self):
        """Get SQLAlchemy engine with connection pooling"""
        if self._engine is None:
            self._engine = create_engine(
                self.config.connection_string,
                poolclass=QueuePool,
                pool_size=self.config.pool_size,
                max_overflow=self.config.max_overflow,
                pool_pre_ping=True,
                echo=False  # Set to True for SQL debugging
            )
        return self._engine
    
    @property
    def session_factory(self):
        """Get SQLAlchemy session factory"""
        if self._session_factory is None:
            self._session_factory = sessionmaker(bind=self.engine)
        return self._session_factory
    
    @contextmanager
    def get_session(self):
        """Get a database session with automatic cleanup"""
        session = self.session_factory()
        try:
            yield session
            session.commit()
        except Exception as e:
            session.rollback()
            logger.error(f"Database session error: {e}")
            raise
        finally:
            session.close()
    
    @contextmanager
    def get_connection(self):
        """Get a raw psycopg2 connection with automatic cleanup"""
        conn = None
        try:
            conn = psycopg2.connect(**self.config.psycopg2_params)
            yield conn
            conn.commit()
        except Exception as e:
            if conn:
                conn.rollback()
            logger.error(f"Database connection error: {e}")
            raise
        finally:
            if conn:
                conn.close()
    
    def test_connection(self) -> bool:
        """Test database connection"""
        try:
            with self.get_connection() as conn:
                with conn.cursor() as cursor:
                    cursor.execute("SELECT 1")
                    result = cursor.fetchone()
                    logger.info("Database connection successful")
                    return result[0] == 1
        except Exception as e:
            logger.error(f"Database connection failed: {e}")
            return False
    
    def execute_query(self, query: str, params: Optional[tuple] = None) -> List[Dict[str, Any]]:
        """Execute a SELECT query and return results as list of dictionaries"""
        try:
            with self.get_connection() as conn:
                with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                    cursor.execute(query, params)
                    results = cursor.fetchall()
                    return [dict(row) for row in results]
        except Exception as e:
            logger.error(f"Query execution failed: {e}")
            raise
    
    def execute_command(self, command: str, params: Optional[tuple] = None) -> int:
        """Execute an INSERT/UPDATE/DELETE command and return affected rows count"""
        try:
            with self.get_connection() as conn:
                with conn.cursor() as cursor:
                    cursor.execute(command, params)
                    return cursor.rowcount
        except Exception as e:
            logger.error(f"Command execution failed: {e}")
            raise
    
    def execute_many(self, command: str, params_list: List[tuple]) -> int:
        """Execute a command multiple times with different parameters"""
        try:
            with self.get_connection() as conn:
                with conn.cursor() as cursor:
                    cursor.executemany(command, params_list)
                    return cursor.rowcount
        except Exception as e:
            logger.error(f"Batch command execution failed: {e}")
            raise


# Pydantic models for data validation
class TransactionCreate(BaseModel):
    """Model for creating a new transaction"""
    ticker: str = Field(..., max_length=20, description="Stock ticker symbol")
    transaction_type: str = Field(default="BUY", description="Transaction type: BUY, SELL, DIVIDEND, SPLIT")
    quantity: Decimal = Field(..., description="Number of shares (as shown by broker)")
    cost_per_share: Decimal = Field(..., description="Price per share")
    currency: str = Field(..., description="Currency: USD, EUR, GBP")
    transaction_date: date = Field(..., description="Transaction date")
    commission: Optional[Decimal] = Field(default=Decimal('0.00'), description="Transaction commission")
    commission_currency: Optional[str] = Field(default=None, description="Commission currency")
    drip_confirmed: bool = Field(default=False, description="DRIP confirmation status")
    
    # Simple Fractional Share Support
    is_fractional: bool = Field(default=False, description="True if this represents fractional shares")
    fractional_multiplier: Decimal = Field(default=Decimal('1.0'), description="Multiplier to get actual shares (quantity * multiplier = real shares)")
    
    notes: Optional[str] = Field(default=None, description="Additional notes")


class TransactionUpdate(BaseModel):
    """Model for updating an existing transaction"""
    ticker: Optional[str] = Field(None, max_length=20)
    transaction_type: Optional[str] = None
    quantity: Optional[Decimal] = None
    cost_per_share: Optional[Decimal] = None
    currency: Optional[str] = None
    transaction_date: Optional[date] = None
    commission: Optional[Decimal] = None
    commission_currency: Optional[str] = None
    drip_confirmed: Optional[bool] = None
    
    # Simple Fractional Share Support
    is_fractional: Optional[bool] = None
    fractional_multiplier: Optional[Decimal] = None
    
    notes: Optional[str] = None


class PositionUpdate(BaseModel):
    """Model for updating position market data"""
    ticker: str = Field(..., max_length=20)
    current_price: Decimal = Field(..., description="Current market price")
    current_market_value: Optional[Decimal] = None
    unrealized_gain_loss: Optional[Decimal] = None
    last_price_update: Optional[datetime] = None


# Global database manager instance
db_manager = DatabaseManager()


def get_db_manager() -> DatabaseManager:
    """Get the global database manager instance"""
    return db_manager


def init_database():
    """Initialize database connection and test it"""
    logger.info("Initializing database connection...")
    if db_manager.test_connection():
        logger.info("Database initialized successfully")
        return True
    else:
        logger.error("Database initialization failed")
        return False


if __name__ == "__main__":
    # Test the database connection
    print("Testing database connection...")
    if init_database():
        print("✅ Database connection successful!")
        
        # Test a simple query
        try:
            results = db_manager.execute_query("SELECT current_database(), current_user, version()")
            print(f"Connected to database: {results[0]['current_database']}")
            print(f"User: {results[0]['current_user']}")
            print(f"PostgreSQL version: {results[0]['version'][:50]}...")
        except Exception as e:
            print(f"❌ Query test failed: {e}")
    else:
        print("❌ Database connection failed!") 