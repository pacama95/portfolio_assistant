#!/usr/bin/env python3
"""
Database Setup Test Script
Tests that the PostgreSQL database is properly initialized with the expected schema.
"""

import sys
import os
from datetime import datetime, date
from decimal import Decimal

# Add parent directories to path to import our modules
sys.path.append(os.path.join(os.path.dirname(__file__), '..', '..'))

try:
    from persistence.database.connection import db_manager, init_database
except ImportError as e:
    print(f"❌ Error importing database modules: {e}")
    print("Make sure you're running this from the project root or database directory")
    print("and that your virtual environment is activated.")
    sys.exit(1)


def print_header(title: str):
    """Print a formatted header"""
    print(f"\n{'=' * 60}")
    print(f" {title}")
    print(f"{'=' * 60}")


def print_section(title: str):
    """Print a formatted section header"""
    print(f"\n{'-' * 40}")
    print(f" {title}")
    print(f"{'-' * 40}")


def test_connection() -> bool:
    """Test basic database connection"""
    print_section("Testing Database Connection")
    
    try:
        if not init_database():
            print("❌ Database connection failed")
            return False
        
        # Test basic query
        result = db_manager.execute_query("SELECT current_database(), current_user, version()")
        if result:
            db_info = result[0]
            print(f"✅ Connected to database: {db_info['current_database']}")
            print(f"✅ Connected as user: {db_info['current_user']}")
            print(f"✅ PostgreSQL version: {db_info['version'][:50]}...")
            return True
        else:
            print("❌ Failed to execute test query")
            return False
            
    except Exception as e:
        print(f"❌ Connection test failed: {e}")
        return False


def test_extensions() -> bool:
    """Test that required extensions are installed"""
    print_section("Testing Database Extensions")
    
    try:
        extensions = db_manager.execute_query("""
            SELECT extname, extversion 
            FROM pg_extension 
            WHERE extname = 'uuid-ossp'
        """)
        
        if extensions:
            for ext in extensions:
                print(f"✅ Extension '{ext['extname']}' version {ext['extversion']} installed")
            return True
        else:
            print("❌ Required extension 'uuid-ossp' not found")
            return False
            
    except Exception as e:
        print(f"❌ Extension test failed: {e}")
        return False


def test_enums() -> bool:
    """Test that custom enum types exist"""
    print_section("Testing Custom Enum Types")
    
    try:
        enums = db_manager.execute_query("""
            SELECT typname, array_agg(enumlabel ORDER BY enumsortorder) as values
            FROM pg_type t 
            JOIN pg_enum e ON t.oid = e.enumtypid 
            WHERE typname IN ('currency_type', 'transaction_type')
            GROUP BY typname
            ORDER BY typname
        """)
        
        expected_enums = {
            'currency_type': ['USD', 'EUR', 'GBP'],
            'transaction_type': ['BUY', 'SELL', 'DIVIDEND', 'SPLIT']
        }
        
        if len(enums) != 2:
            print(f"❌ Expected 2 enums, found {len(enums)}")
            return False
        
        for enum in enums:
            enum_name = enum['typname']
            enum_values = enum['values']
            expected_values = expected_enums.get(enum_name, [])
            
            if set(enum_values) == set(expected_values):
                print(f"✅ Enum '{enum_name}': {enum_values}")
            else:
                print(f"❌ Enum '{enum_name}' values mismatch")
                print(f"   Expected: {expected_values}")
                print(f"   Found: {enum_values}")
                return False
        
        return True
        
    except Exception as e:
        print(f"❌ Enum test failed: {e}")
        return False


def test_tables() -> bool:
    """Test that all expected tables exist with correct structure"""
    print_section("Testing Database Tables")
    
    try:
        # Get all tables
        tables = db_manager.execute_query("""
            SELECT table_name 
            FROM information_schema.tables 
            WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
            ORDER BY table_name
        """)
        
        table_names = [t['table_name'] for t in tables]
        expected_tables = ['transactions', 'positions']
        
        print(f"Found {len(table_names)} tables: {table_names}")
        
        # Check expected tables exist
        for table in expected_tables:
            if table in table_names:
                print(f"✅ Table '{table}' exists")
                
                # Test table structure
                columns = db_manager.execute_query(f"""
                    SELECT column_name, data_type, is_nullable, column_default
                    FROM information_schema.columns 
                    WHERE table_name = '{table}' AND table_schema = 'public'
                    ORDER BY ordinal_position
                """)
                
                print(f"   - {len(columns)} columns defined")
                
                # Check for required columns
                column_names = [c['column_name'] for c in columns]
                if table == 'transactions':
                    required_cols = ['id', 'ticker', 'transaction_type', 'quantity', 'cost_per_share']
                elif table == 'positions':
                    required_cols = ['id', 'ticker', 'current_quantity', 'avg_cost_per_share']
                
                missing_cols = [col for col in required_cols if col not in column_names]
                if missing_cols:
                    print(f"❌ Missing required columns in {table}: {missing_cols}")
                    return False
                
            else:
                print(f"❌ Required table '{table}' missing")
                return False
        
        return True
        
    except Exception as e:
        print(f"❌ Table test failed: {e}")
        return False


def test_views() -> bool:
    """Test that all expected views exist"""
    print_section("Testing Database Views")
    
    try:
        views = db_manager.execute_query("""
            SELECT table_name 
            FROM information_schema.views 
            WHERE table_schema = 'public'
            ORDER BY table_name
        """)
        
        view_names = [v['table_name'] for v in views]
        expected_views = ['portfolio_summary', 'position_details']
        
        print(f"Found {len(view_names)} views: {view_names}")
        
        for view in expected_views:
            if view in view_names:
                print(f"✅ View '{view}' exists")
                
                # Test that view can be queried
                try:
                    result = db_manager.execute_query(f"SELECT * FROM {view} LIMIT 1")
                    print(f"   - View '{view}' is queryable")
                except Exception as e:
                    print(f"❌ View '{view}' query failed: {e}")
                    return False
            else:
                print(f"❌ Required view '{view}' missing")
                return False
        
        return True
        
    except Exception as e:
        print(f"❌ View test failed: {e}")
        return False


def test_functions() -> bool:
    """Test that stored functions exist"""
    print_section("Testing Database Functions")
    
    try:
        functions = db_manager.execute_query("""
            SELECT routine_name, routine_type 
            FROM information_schema.routines 
            WHERE routine_schema = 'public' AND routine_type = 'FUNCTION'
            ORDER BY routine_name
        """)
        
        function_names = [f['routine_name'] for f in functions]
        expected_functions = ['recalculate_position', 'trigger_recalculate_position', 'update_updated_at_column']
        
        print(f"Found {len(function_names)} functions: {function_names}")
        
        for func in expected_functions:
            if func in function_names:
                print(f"✅ Function '{func}' exists")
            else:
                print(f"❌ Required function '{func}' missing")
                return False
        
        return True
        
    except Exception as e:
        print(f"❌ Function test failed: {e}")
        return False


def test_indexes() -> bool:
    """Test that expected indexes exist"""
    print_section("Testing Database Indexes")
    
    try:
        indexes = db_manager.execute_query("""
            SELECT indexname, tablename 
            FROM pg_indexes 
            WHERE schemaname = 'public'
            AND indexname NOT LIKE '%_pkey'  -- Exclude primary key indexes
            ORDER BY tablename, indexname
        """)
        
        index_names = [i['indexname'] for i in indexes]
        expected_indexes = [
            'idx_transactions_ticker',
            'idx_transactions_date', 
            'idx_transactions_ticker_date',
            'idx_positions_ticker'
        ]
        
        print(f"Found {len(index_names)} custom indexes: {index_names}")
        
        for idx in expected_indexes:
            if idx in index_names:
                print(f"✅ Index '{idx}' exists")
            else:
                print(f"❌ Expected index '{idx}' missing")
                return False
        
        return True
        
    except Exception as e:
        print(f"❌ Index test failed: {e}")
        return False


def test_sample_operations() -> bool:
    """Test basic CRUD operations"""
    print_section("Testing Sample Database Operations")
    
    try:
        # Test INSERT
        print("Testing INSERT operation...")
        insert_result = db_manager.execute_command("""
            INSERT INTO transactions (ticker, transaction_type, quantity, cost_per_share, currency, transaction_date)
            VALUES (%s, %s, %s, %s, %s, %s)
        """, ('TEST', 'BUY', Decimal('10'), Decimal('100.50'), 'USD', date.today()))
        
        if insert_result == 1:
            print("✅ Insert operation successful")
        else:
            print(f"❌ Insert operation failed, affected rows: {insert_result}")
            return False
        
        # Test SELECT
        print("Testing SELECT operation...")
        select_result = db_manager.execute_query("""
            SELECT ticker, quantity, cost_per_share FROM transactions WHERE ticker = 'TEST'
        """)
        
        if select_result and len(select_result) == 1:
            row = select_result[0]
            print(f"✅ Select operation successful: {row['ticker']} - {row['quantity']} @ ${row['cost_per_share']}")
        else:
            print(f"❌ Select operation failed or unexpected result: {select_result}")
            return False
        
        # Test that trigger created position
        print("Testing automatic position calculation...")
        position_result = db_manager.execute_query("""
            SELECT ticker, current_quantity, avg_cost_per_share FROM positions WHERE ticker = 'TEST'
        """)
        
        if position_result and len(position_result) == 1:
            pos = position_result[0]
            print(f"✅ Position auto-created: {pos['ticker']} - {pos['current_quantity']} @ ${pos['avg_cost_per_share']}")
        else:
            print("❌ Position was not automatically created by trigger")
            return False
        
        # Test DELETE (cleanup)
        print("Testing DELETE operation...")
        delete_result = db_manager.execute_command("""
            DELETE FROM transactions WHERE ticker = 'TEST'
        """)
        
        if delete_result == 1:
            print("✅ Delete operation successful")
            
            # Verify position was also deleted by trigger
            pos_check = db_manager.execute_query("SELECT * FROM positions WHERE ticker = 'TEST'")
            if not pos_check:
                print("✅ Position auto-deleted by trigger")
            else:
                print("❌ Position was not automatically deleted")
                return False
        else:
            print(f"❌ Delete operation failed, affected rows: {delete_result}")
            return False
        
        return True
        
    except Exception as e:
        print(f"❌ Sample operations test failed: {e}")
        return False


def run_all_tests() -> bool:
    """Run all database tests"""
    print_header("Portfolio Database Setup Test")
    print(f"Test started at: {datetime.now()}")
    
    tests = [
        ("Database Connection", test_connection),
        ("Database Extensions", test_extensions),
        ("Custom Enum Types", test_enums),
        ("Database Tables", test_tables),
        ("Database Views", test_views),
        ("Stored Functions", test_functions),
        ("Database Indexes", test_indexes),
        ("Sample Operations", test_sample_operations),
    ]
    
    passed = 0
    total = len(tests)
    
    for test_name, test_func in tests:
        try:
            if test_func():
                passed += 1
            else:
                print(f"\n❌ {test_name} test FAILED")
        except Exception as e:
            print(f"\n❌ {test_name} test ERROR: {e}")
    
    # Summary
    print_header("Test Results Summary")
    print(f"Tests passed: {passed}/{total}")
    print(f"Tests failed: {total - passed}/{total}")
    
    if passed == total:
        print("\n🎉 All tests PASSED! Your database is properly configured.")
        print("\nYou can now:")
        print("  - Connect to Adminer at http://localhost:8080")
        print("  - Start building your portfolio application")
        print("  - Use the connection module to interact with the database")
        return True
    else:
        print(f"\n❌ {total - passed} tests FAILED. Please check the errors above.")
        print("\nTroubleshooting:")
        print("  - Ensure Docker containers are running: docker-compose ps")
        print("  - Check container logs: docker-compose logs postgres")
        print("  - Try resetting: docker-compose down -v && docker-compose up -d")
        return False


def main():
    """Main function"""
    try:
        success = run_all_tests()
        sys.exit(0 if success else 1)
    except KeyboardInterrupt:
        print("\n\nTest interrupted by user")
        sys.exit(1)
    except Exception as e:
        print(f"\n\nUnexpected error: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main() 