#!/usr/bin/env python3
"""
Test script to check if all imports work correctly
"""

import sys
import os
from pathlib import Path

# Add current directory to path for imports
sys.path.insert(0, str(Path(__file__).parent))

def test_imports():
    """Test all the imports needed for the MCP server"""
    try:
        print("Testing imports...")
        
        # Test portfolio_operations import
        print("1. Testing portfolio_operations import...")
        from server.portfolio_operations import (
            CreateTransactionInput,
            UpdateTransactionInput, 
            UpdateMarketDataInput,
            SearchTransactionsInput
        )
        print("✅ portfolio_operations import successful")
        
        # Test portfolio_server import
        print("2. Testing portfolio_server import...")
        from server.portfolio_server import PORTFOLIO_TOOLS
        print(f"✅ portfolio_server import successful - {len(PORTFOLIO_TOOLS)} tools found")
        
        # Test portfolio_service import
        print("3. Testing portfolio_service import...")
        from server.portfolio_service import portfolio_service
        print("✅ portfolio_service import successful")
        
        # Test persistence import
        print("4. Testing persistence import...")
        from persistence import get_db_manager
        print("✅ persistence import successful")
        
        print("\n🎉 All imports successful!")
        return True
        
    except Exception as e:
        print(f"❌ Import failed: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    success = test_imports()
    sys.exit(0 if success else 1) 