#!/bin/bash

echo "🧪 Testing All Portfolio Tools via HTTP API"
echo "============================================="
echo "Server: http://localhost:8081"
echo ""

# Check if server is running
echo "1. Checking server health..."
curl -s http://localhost:8081/health | jq '.'
echo ""

echo "2. Getting server information..."
curl -s http://localhost:8081/ | jq '.'
echo ""

echo "3. Listing all available tools..."
curl -s http://localhost:8081/tools | jq '.tools[] | {name: .name, description: .description}'
echo ""

echo "4. Testing portfolio summary..."
curl -s -X POST http://localhost:8081/tools/get_portfolio_summary_tool \
  -H "Content-Type: application/json" \
  -d '{}' | jq '.'
echo ""

echo "5. Testing all positions..."
curl -s -X POST http://localhost:8081/tools/get_all_positions_tool \
  -H "Content-Type: application/json" \
  -d '{}' | jq '.'
echo ""

echo "6. Testing position by ticker (AAPL)..."
curl -s -X POST http://localhost:8081/tools/get_position_by_ticker_tool \
  -H "Content-Type: application/json" \
  -d '{"ticker": "AAPL"}' | jq '.'
echo ""

echo "7. Testing transactions by ticker (AAPL)..."
curl -s -X POST http://localhost:8081/tools/get_transactions_by_ticker_tool \
  -H "Content-Type: application/json" \
  -d '{"ticker": "AAPL"}' | jq '.'
echo ""

echo "8. Testing performance metrics..."
curl -s -X POST http://localhost:8081/tools/get_performance_metrics_tool \
  -H "Content-Type: application/json" \
  -d '{}' | jq '.'
echo ""

echo "9. Testing ticker analysis (AAPL)..."
curl -s -X POST http://localhost:8081/tools/get_ticker_analysis_tool \
  -H "Content-Type: application/json" \
  -d '{"ticker": "AAPL"}' | jq '.'
echo ""

echo "10. Testing search transactions..."
curl -s -X POST http://localhost:8081/tools/search_transactions_tool \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL",
    "start_date": "2024-01-01",
    "end_date": "2024-12-31"
  }' | jq '.'
echo ""

echo "🎉 Basic read-only tests completed!"
echo ""
echo "⚠️  WRITE OPERATIONS (Not executed to avoid modifying data):"
echo "   • create_transaction_tool - Creates new transactions"
echo "   • update_transaction_tool - Updates existing transactions"  
echo "   • delete_transaction_tool - Deletes transactions"
echo "   • update_market_data_tool - Updates market prices"
echo "   • recalculate_position_tool - Recalculates positions"
echo "   • recalculate_all_positions_tool - Recalculates all positions"
echo ""
echo "📝 To test write operations, use the individual curl commands from the documentation"
echo "📚 See: mcp_persistence/docs/HTTP_SERVER_USAGE.md" 