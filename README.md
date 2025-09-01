# 📈 Portfolio Assistant

**A comprehensive AI-powered portfolio management system combining enterprise-grade backend infrastructure with intelligent AI agents for stocks, crypto, funds, and commodities analysis.**

## 🎯 Project Overview

This project provides a complete portfolio management solution with two main components:

1. **🏗️ Enterprise Backend**: A robust Quarkus-based microservice with PostgreSQL database, REST API, and MCP (Model Context Protocol) server
2. **🤖 AI Agent System**: Intelligent Python-based tools and agents for portfolio analysis, document processing, and market insights

The system enables users to manage their investment portfolios while leveraging AI capabilities for market analysis, document understanding, and intelligent decision support.

## 🛠️ Tech Stack

### Backend Infrastructure
| Component | Technology |
|-----------|------------|
| **Framework** | Quarkus (Reactive Java) |
| **Language** | Java 21 |
| **Database** | PostgreSQL 14+ |
| **ORM** | Hibernate Reactive with Panache |
| **API** | JAX-RS (REST) + MCP Protocol |
| **Build Tool** | Gradle |
| **Testing** | JUnit 5 + TestContainers |

### AI/ML Stack
| Component | Technology |
|-----------|------------|
| **AI Framework** | LangChain + LangGraph |
| **Protocol** | MCP (Model Context Protocol) |
| **UI Framework** | Gradio |
| **Language** | Python 3.9+ |
| **ML Models** | HuggingFace Transformers |
| **Data Analysis** | Pandas, NumPy |
| **Market Data** | Yahoo Finance API |

## 📁 Project Structure

```
portfolio_assistant/
├── 🏗️ BACKEND SERVICES
│   └── mcp_persistence_quarkus/          # Enterprise portfolio backend
│       ├── src/main/java/com/portfolio/
│       │   ├── application/              # Business use cases
│       │   │   ├── usecase/             # Transaction, position, portfolio logic
│       │   │   └── command/             # Command DTOs
│       │   ├── domain/                  # Core business models
│       │   │   ├── model/              # Portfolio, transaction, position entities
│       │   │   ├── port/               # Repository interfaces
│       │   │   └── exception/          # Business exceptions
│       │   └── infrastructure/         # External integrations
│       │       ├── persistence/        # PostgreSQL + Hibernate
│       │       ├── rest/               # HTTP REST API
│       │       ├── mcp/                # MCP server for AI integration
│       │       └── marketdata/         # Market data adapters
│       ├── src/main/resources/
│       │   ├── application.properties  # Quarkus configuration
│       │   └── db/migration/           # Database schema & migrations
│       ├── scripts/                    # Testing & deployment scripts
│       ├── build.gradle               # Gradle build configuration
│       └── docker-compose.yml         # PostgreSQL containerization
│
├── 🤖 AI AGENT SYSTEM
│   ├── app.py                         # Main Gradio chat interface
│   ├── tools/                         # AI-powered analysis tools
│   │   ├── document_question_answering_tool.py  # Document AI analysis
│   │   ├── document_reader.py         # PDF/document processing
│   │   ├── image_analyzer.py          # Financial chart analysis
│   │   ├── video_analyzer.py          # Video content analysis
│   │   └── youtube_video_transcript.py # YouTube financial content
│   ├── context_window_optimizer.py   # LLM context optimization
│   └── requirements.txt               # Python dependencies
│
├── 📊 ANALYSIS & NOTEBOOKS
│   ├── Portfolio Assistant.ipynb     # Main portfolio analysis notebook
│   ├── Calculate position from CSV.ipynb  # Position calculation tools
│   ├── Stocks Agents.ipynb          # Stock analysis agents
│   └── DivTracker_Default_*.csv     # Sample portfolio data
│
├── 📂 ADDITIONAL MODULES
│   ├── mcp_market_analysis/         # Market analysis MCP tools (placeholder)
│   ├── portfolio_persistence/       # Additional persistence layer (placeholder)
│   ├── PROJECT_STRUCTURE.md        # Detailed project documentation
│   └── prompt_for_problem_resolution.md  # Problem-solving prompts
```

## 🚀 Features

### 💼 Portfolio Management Backend
- ✅ **Transaction Management**: Create, read, update, delete transactions
- ✅ **Position Tracking**: Automatic position calculation from transactions
- ✅ **Real-time Market Data**: Price updates and P&L calculations
- ✅ **Portfolio Analytics**: Comprehensive portfolio metrics and summaries
- ✅ **Multi-Currency Support**: USD, EUR, GBP with proper conversion
- ✅ **Dividend Tracking**: Dividend history and yield calculations
- ✅ **RESTful API**: Complete HTTP API for web/mobile integration
- ✅ **MCP Protocol**: AI-native interface for intelligent agents

### 🤖 AI Agent Capabilities
- ✅ **Conversational Interface**: Natural language portfolio queries
- ✅ **Document Analysis**: Extract insights from financial documents
- ✅ **Image Recognition**: Analyze financial charts and screenshots
- ✅ **Video Processing**: Extract information from financial videos
- ✅ **Market Research**: YouTube transcript analysis for market insights
- ✅ **Context Optimization**: Intelligent context window management
- ✅ **Multi-Modal Analysis**: Text, image, and video understanding

### 🔄 Integration Features
- ✅ **Reactive Architecture**: Non-blocking, high-performance backend
- ✅ **Clean Architecture**: Separated concerns and testable design
- ✅ **MCP Integration**: Seamless AI-backend communication
- ✅ **Database Triggers**: Automatic position recalculation
- ✅ **Error Handling**: Comprehensive error management and validation

## 🚀 Quick Start

### Prerequisites
- **Java 21+** (for backend)
- **Python 3.9+** (for AI agents)
- **PostgreSQL 14+** (for data persistence)
- **Docker** (optional, for database)

### 1. Backend Setup

```bash
# Navigate to backend directory
cd mcp_persistence_quarkus

# Start PostgreSQL (using Docker)
docker-compose up -d

# Run backend in development mode
./gradlew quarkusDev
```

The backend will be available at:
- REST API: `http://localhost:8080`
- Health checks: `http://localhost:8080/q/health`
- API docs: `http://localhost:8080/q/swagger-ui`

### 2. AI Agent Setup

```bash
# Install Python dependencies
pip install -r requirements.txt

# Start the Gradio interface
python app.py
```

The AI interface will be available at: `http://localhost:7860`

### 3. Verify Installation

```bash
# Test backend API
cd mcp_persistence_quarkus
./scripts/quick-test.sh

# Test MCP server
./scripts/test-mcp.sh
```

## 📡 API Reference

### 🌐 REST API Endpoints

#### Transactions
- `POST /api/transactions` - Create new transaction
- `GET /api/transactions` - List all transactions
- `GET /api/transactions/{id}` - Get specific transaction
- `PUT /api/transactions/{id}` - Update transaction
- `DELETE /api/transactions/{id}` - Delete transaction

#### Positions
- `GET /api/positions` - List all positions
- `GET /api/positions/active` - List active positions only
- `GET /api/positions/ticker/{ticker}` - Get position by ticker
- `PUT /api/positions/ticker/{ticker}/price` - Update market price

#### Portfolio
- `GET /api/portfolio/summary` - Complete portfolio summary
- `GET /api/portfolio/summary/active` - Active positions summary

### 🤖 MCP Tools (AI Integration)

The backend exposes MCP tools for AI agents:
- `create_transaction` - Create portfolio transactions
- `get_portfolio_summary` - Retrieve portfolio analytics
- `get_position_by_ticker` - Get specific stock positions
- `update_market_price` - Update current market prices
- `recalculate_position` - Refresh position calculations

## 💡 Usage Examples

### Portfolio Management
```bash
# Add a stock purchase
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL",
    "transactionType": "BUY",
    "quantity": 10,
    "price": 150.00,
    "currency": "USD",
    "transactionDate": "2024-01-15"
  }'

# Get portfolio summary
curl http://localhost:8080/api/portfolio/summary/active
```

### AI Agent Interaction
```python
# Using the MCP client for AI integration
async with PortfolioMcpClient() as client:
    # AI can create transactions
    result = await client.call_tool('create_transaction', {
        "ticker": "AAPL",
        "transactionType": "BUY",
        "quantity": 10,
        "price": 150.00
    })
    
    # AI can analyze portfolio
    summary = await client.call_tool('get_portfolio_summary', {})
```

## 🧪 Testing

### Backend Testing
```bash
cd mcp_persistence_quarkus

# Run unit tests
./gradlew test

# Integration tests
./gradlew integrationTest

# API testing
./scripts/test-api.sh
```

### AI Agent Testing
```bash
# Test individual tools
python -m tools.document_question_answering_tool

# Test MCP integration
./mcp_persistence_quarkus/scripts/test-mcp.sh
```

## 🔧 Configuration

### Backend Configuration
Edit `mcp_persistence_quarkus/src/main/resources/application.properties`:
```properties
# Database
quarkus.datasource.reactive.url=postgresql://localhost:5432/portfolio_db
quarkus.datasource.username=portfolio_user
quarkus.datasource.password=your_password

# Server
quarkus.http.port=8080
```

### AI Agent Configuration
Edit environment variables or `app.py`:
```python
# HuggingFace model configuration
client = InferenceClient("HuggingFaceH4/zephyr-7b-beta")

# Gradio interface settings
demo.launch(server_port=7860, share=False)
```

## 🚀 Deployment

### Docker Deployment
```bash
# Backend
cd mcp_persistence_quarkus
docker-compose up -d

# AI Agent
docker build -t portfolio-ai .
docker run -p 7860:7860 portfolio-ai
```

### Production Configuration
- Configure PostgreSQL with production credentials
- Set up reverse proxy (nginx) for both services
- Configure environment-specific settings
- Set up monitoring and logging

## 🤝 Development

### Adding New Features

1. **Backend Features**: Follow clean architecture in `mcp_persistence_quarkus/`
   - Domain models → Application use cases → Infrastructure adapters
   
2. **AI Tools**: Add new tools in `tools/` directory
   - Inherit from `BaseTool` for LangChain integration
   - Implement MCP protocol for backend communication

3. **Integration**: Use MCP protocol for AI-backend communication
   - Backend exposes MCP tools
   - AI agents consume tools via MCP client

## 📚 Documentation

- **Backend API**: Access Swagger UI at `/q/swagger-ui` when backend is running
- **Health Monitoring**: Health checks at `/q/health`
- **MCP Tools**: MCP tool registry at `/mcp/tools`
- **Project Structure**: See `PROJECT_STRUCTURE.md` for detailed architecture

## 🛡️ Security & Best Practices

- ✅ **Input Validation**: Bean validation on all DTOs
- ✅ **SQL Injection Protection**: Parameterized queries
- ✅ **Error Handling**: Consistent error responses
- ✅ **Clean Architecture**: Separated concerns and testable design
- ✅ **Reactive Programming**: Non-blocking I/O for scalability

## 🔮 Roadmap

- [ ] **Authentication & Authorization** for multi-user support
- [ ] **Real-time WebSocket** updates for live portfolio tracking
- [ ] **Advanced AI Models** for market prediction and analysis
- [ ] **Mobile App Integration** via REST API
- [ ] **Advanced Analytics** with ML-powered insights
- [ ] **Multi-broker Integration** for automatic trade execution
- [ ] **Risk Management** tools and alerts

---

**Built with ❤️ using Quarkus, LangChain, and MCP Protocol**

> This project demonstrates modern software architecture combining enterprise Java backends with intelligent Python AI agents, connected through the Model Context Protocol for seamless AI-native integration.