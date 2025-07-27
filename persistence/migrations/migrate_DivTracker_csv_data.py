"""
Data Migration Script for Portfolio Management System
Migrates existing CSV transaction data to PostgreSQL database
"""

import os
import sys
import pandas as pd
from datetime import datetime, date
from decimal import Decimal
from typing import List, Dict, Any
import logging

# Add project root to Python path
project_root = os.path.join(os.path.dirname(__file__), '..', '..')
if project_root not in sys.path:
    sys.path.insert(0, project_root)

from persistence.database.connection import init_database, get_db_manager, TransactionCreate
from persistence.crud.portfolio_crud import portfolio_crud

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class CSVDataMigrator:
    """Handles migration of CSV portfolio data to PostgreSQL"""
    
    def __init__(self):
        self.db = get_db_manager()
        self.transaction_crud = portfolio_crud.transactions
        self.position_crud = portfolio_crud.positions
        
    def clean_and_validate_csv_data(self, df: pd.DataFrame) -> pd.DataFrame:
        """Clean and validate CSV data before migration"""
        logger.info(f"Cleaning CSV data with {len(df)} rows")
        
        # Make a copy to avoid modifying original
        cleaned_df = df.copy()
        
        # Convert Date column to datetime
        cleaned_df['Date'] = pd.to_datetime(cleaned_df['Date'])
        
        # Fill NaN commissions with 0
        cleaned_df['Commission'] = cleaned_df['Commission'].fillna(0)
        
        # Clean ticker symbols (remove spaces, convert to uppercase)
        cleaned_df['Ticker'] = cleaned_df['Ticker'].str.strip().str.upper()
        
        # Determine transaction type based on quantity
        cleaned_df['Transaction_Type'] = cleaned_df['Quantity'].apply(
            lambda x: 'BUY' if x > 0 else 'SELL'
        )
        
        # Ensure currencies are uppercase
        cleaned_df['Currency'] = cleaned_df['Currency'].str.upper()
        cleaned_df['Commission Currency'] = cleaned_df['Commission Currency'].fillna('').str.upper()
        
        # Handle DRIP confirmed column
        cleaned_df['DRIP Confirmed'] = cleaned_df['DRIP Confirmed'].fillna(False)
        cleaned_df['DRIP Confirmed'] = cleaned_df['DRIP Confirmed'].astype(bool)
        
        # Remove any rows with missing essential data
        essential_columns = ['Ticker', 'Quantity', 'Cost Per Share', 'Currency', 'Date']
        initial_rows = len(cleaned_df)
        cleaned_df = cleaned_df.dropna(subset=essential_columns)
        dropped_rows = initial_rows - len(cleaned_df)
        
        if dropped_rows > 0:
            logger.warning(f"Dropped {dropped_rows} rows due to missing essential data")
        
        # Sort by date to ensure proper order
        cleaned_df = cleaned_df.sort_values(['Ticker', 'Date'])
        
        logger.info(f"Data cleaning complete. {len(cleaned_df)} rows ready for migration")
        return cleaned_df
    
    def convert_csv_row_to_transaction(self, row: pd.Series) -> TransactionCreate:
        """Convert a CSV row to a TransactionCreate object"""
        
        # Handle commission currency - use transaction currency if commission currency is empty
        commission_currency = row['Commission Currency'] if row['Commission Currency'] else row['Currency']
        if commission_currency == '':
            commission_currency = row['Currency']
        
        return TransactionCreate(
            ticker=row['Ticker'],
            transaction_type=row['Transaction_Type'],
            quantity=Decimal(str(abs(row['Quantity']))),  # Store as positive, type indicates buy/sell
            cost_per_share=Decimal(str(row['Cost Per Share'])),
            currency=row['Currency'],
            transaction_date=row['Date'].date(),
            commission=Decimal(str(row['Commission'])) if row['Commission'] > 0 else Decimal('0'),
            commission_currency=commission_currency if commission_currency else None,
            drip_confirmed=bool(row['DRIP Confirmed']),
            notes=f"Migrated from CSV on {datetime.now().strftime('%Y-%m-%d')}"
        )
    
    def migrate_csv_file(self, csv_file_path: str, batch_size: int = 100) -> Dict[str, Any]:
        """Migrate data from CSV file to database"""
        if not os.path.exists(csv_file_path):
            raise FileNotFoundError(f"CSV file not found: {csv_file_path}")
        
        logger.info(f"Starting migration from {csv_file_path}")
        
        # Read CSV file
        try:
            df = pd.read_csv(csv_file_path)
            logger.info(f"Loaded CSV file with {len(df)} rows")
        except Exception as e:
            logger.error(f"Failed to read CSV file: {e}")
            raise
        
        # Clean and validate data
        df = self.clean_and_validate_csv_data(df)
        
        # Convert to TransactionCreate objects
        transactions = []
        failed_conversions = []
        
        for index, row in df.iterrows():
            try:
                transaction = self.convert_csv_row_to_transaction(row)
                transactions.append(transaction)
            except Exception as e:
                logger.error(f"Failed to convert row {index}: {e}")
                failed_conversions.append(index)
        
        logger.info(f"Converted {len(transactions)} transactions, {len(failed_conversions)} failed")
        
        # Migrate in batches
        migrated_count = 0
        failed_migrations = []
        
        for i in range(0, len(transactions), batch_size):
            batch = transactions[i:i + batch_size]
            logger.info(f"Migrating batch {i//batch_size + 1} ({len(batch)} transactions)")
            
            try:
                # Use bulk create for efficiency
                transaction_ids = self.transaction_crud.bulk_create_transactions(batch)
                migrated_count += len(transaction_ids)
                logger.info(f"Successfully migrated batch with {len(transaction_ids)} transactions")
                
            except Exception as e:
                logger.error(f"Failed to migrate batch: {e}")
                # Try individual transactions in this batch
                for j, transaction in enumerate(batch):
                    try:
                        transaction_id = self.transaction_crud.create_transaction(transaction)
                        migrated_count += 1
                    except Exception as individual_error:
                        logger.error(f"Failed to migrate individual transaction: {individual_error}")
                        failed_migrations.append(i + j)
        
        # Recalculate all positions after migration
        logger.info("Recalculating positions from migrated transactions...")
        self.position_crud.recalculate_all_positions()
        
        migration_summary = {
            'total_csv_rows': len(df),
            'conversion_failures': len(failed_conversions),
            'successful_migrations': migrated_count,
            'migration_failures': len(failed_migrations),
            'failed_conversion_indices': failed_conversions,
            'failed_migration_indices': failed_migrations
        }
        
        logger.info("Migration completed!")
        logger.info(f"Summary: {migration_summary}")
        
        return migration_summary
    
    def verify_migration(self, csv_file_path: str) -> Dict[str, Any]:
        """Verify the migration by comparing CSV data with database data"""
        logger.info("Verifying migration...")
        
        # Load original CSV
        df = pd.read_csv(csv_file_path)
        df = self.clean_and_validate_csv_data(df)
        
        # Get database transactions
        db_transactions = self.transaction_crud.get_all_transactions()
        
        # Compare counts
        csv_transactions = len(df)
        db_transaction_count = len(db_transactions)
        
        # Get unique tickers
        csv_tickers = set(df['Ticker'].unique())
        db_tickers = set(t['ticker'] for t in db_transactions)
        
        # Get positions count
        positions = self.position_crud.get_all_positions()
        
        verification_result = {
            'csv_transaction_count': csv_transactions,
            'db_transaction_count': db_transaction_count,
            'count_match': csv_transactions == db_transaction_count,
            'csv_unique_tickers': len(csv_tickers),
            'db_unique_tickers': len(db_tickers),
            'ticker_match': csv_tickers == db_tickers,
            'missing_tickers': csv_tickers - db_tickers,
            'extra_tickers': db_tickers - csv_tickers,
            'current_positions': len(positions),
            'positions_list': [p['ticker'] for p in positions]
        }
        
        logger.info(f"Verification complete: {verification_result}")
        return verification_result
    
    def get_migration_summary_report(self) -> str:
        """Generate a detailed migration summary report"""
        try:
            # Get portfolio summary
            summary = self.position_crud.get_portfolio_summary()
            positions = self.position_crud.get_all_positions()
            transactions = self.transaction_crud.get_all_transactions()
            
            report = f"""
=== PORTFOLIO MIGRATION SUMMARY REPORT ===
Generated on: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}

DATABASE STATISTICS:
- Total Transactions: {len(transactions)}
- Current Positions: {len(positions)}
- Total Portfolio Value: ${summary.get('total_market_value', 0):,.2f}
- Total Cost Basis: ${summary.get('total_cost_basis', 0):,.2f}
- Total Unrealized P&L: ${summary.get('total_unrealized_gain_loss', 0):,.2f}
- Total Return: {summary.get('total_return_percentage', 0):.2f}%

CURRENT POSITIONS:
"""
            
            for position in positions:
                market_value = float(position.get('current_market_value', 0))
                cost_basis = float(position.get('total_cost_basis', 0))
                unrealized_pnl = market_value - cost_basis
                return_pct = (unrealized_pnl / cost_basis * 100) if cost_basis > 0 else 0
                
                report += f"- {position['ticker']}: {position['current_quantity']:.2f} shares, "
                report += f"Market Value: ${market_value:,.2f}, "
                report += f"P&L: ${unrealized_pnl:,.2f} ({return_pct:+.2f}%)\n"
            
            return report
            
        except Exception as e:
            logger.error(f"Failed to generate summary report: {e}")
            return f"Error generating report: {e}"


def main():
    """Main migration function"""
    # Find CSV files in project root directory
    project_root = os.path.join(os.path.dirname(__file__), '..', '..')
    csv_files = [f for f in os.listdir(project_root) if f.endswith('.csv') and 'DivTracker' in f]
    
    if not csv_files:
        print("No DivTracker CSV files found in project root directory")
        print(f"Looking in: {os.path.abspath(project_root)}")
        return
    
    # Use the first CSV file found (or specify manually)
    csv_file = os.path.join(project_root, csv_files[0])
    print(f"Found CSV file: {csv_file}")
    
    # Initialize database
    print("Initializing database connection...")
    if not init_database():
        print("❌ Database initialization failed!")
        return
    
    print("✅ Database connection successful!")
    
    # Create migrator and run migration
    migrator = CSVDataMigrator()
    
    try:
        # Run migration
        print(f"\n🚀 Starting migration from {csv_file}...")
        migration_result = migrator.migrate_csv_file(csv_file)
        
        print("\n📊 Migration Results:")
        print(f"- Total CSV rows: {migration_result['total_csv_rows']}")
        print(f"- Successful migrations: {migration_result['successful_migrations']}")
        print(f"- Failed conversions: {migration_result['conversion_failures']}")
        print(f"- Failed migrations: {migration_result['migration_failures']}")
        
        # Verify migration
        print("\n🔍 Verifying migration...")
        verification = migrator.verify_migration(csv_file)
        
        if verification['count_match'] and verification['ticker_match']:
            print("✅ Migration verification passed!")
        else:
            print("⚠️ Migration verification found discrepancies:")
            if not verification['count_match']:
                print(f"  - Transaction count mismatch: CSV={verification['csv_transaction_count']}, DB={verification['db_transaction_count']}")
            if not verification['ticker_match']:
                print(f"  - Ticker mismatch: Missing={verification['missing_tickers']}, Extra={verification['extra_tickers']}")
        
        # Generate summary report
        print("\n📈 Portfolio Summary:")
        report = migrator.get_migration_summary_report()
        print(report)
        
        print("\n🎉 Migration completed successfully!")
        
    except Exception as e:
        print(f"❌ Migration failed: {e}")
        logger.error(f"Migration error: {e}")


if __name__ == "__main__":
    main() 