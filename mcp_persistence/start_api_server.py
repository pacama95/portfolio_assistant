#!/usr/bin/env python3
"""
Portfolio API Server Launcher
Simple script to start the FastAPI portfolio management server
"""

import argparse
import sys
import os

# Setup import paths
current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
sys.path.insert(0, current_dir)  # Add mcp_persistence to path
sys.path.insert(0, parent_dir)   # Add root directory to path

from server.portfolio_api import run_server

def main():
    parser = argparse.ArgumentParser(description="Start the Portfolio Management API server")
    parser.add_argument(
        "--host", 
        default="0.0.0.0", 
        help="Host to bind the server to (default: 0.0.0.0)"
    )
    parser.add_argument(
        "--port", 
        type=int, 
        default=8000, 
        help="Port to bind the server to (default: 8000)"
    )
    parser.add_argument(
        "--reload",
        action="store_true",
        help="Enable auto-reload for development"
    )
    
    args = parser.parse_args()
    
    print(f"Starting Portfolio Management API server...")
    print(f"Host: {args.host}")
    print(f"Port: {args.port}")
    print(f"API Documentation: http://{args.host}:{args.port}/docs")
    print(f"API Health Check: http://{args.host}:{args.port}/health")
    print()
    
    # Import uvicorn here to handle import gracefully
    try:
        import uvicorn
        
        # Run the server with optional reload
        uvicorn.run(
            "server.portfolio_api:app",
            host=args.host,
            port=args.port,
            reload=args.reload
        )
    except ImportError:
        print("Error: uvicorn is not installed. Please install it with:")
        print("pip install uvicorn")
        sys.exit(1)
    except KeyboardInterrupt:
        print("\nServer stopped by user")
    except Exception as e:
        print(f"Error starting server: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main() 