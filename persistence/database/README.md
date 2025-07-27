# Portfolio Database Setup Guide

This directory contains the PostgreSQL database configuration for the Portfolio Management System.

## Quick Start

### Prerequisites
- Docker and Docker Compose installed
- Python virtual environment with required dependencies

### First Time Setup

1. **Start the database containers:**
   ```bash
   cd persistence/database
   docker-compose up -d
   ```

2. **Verify the setup:**
   ```bash
   # Run the test script
   python test_db_setup.py
   ```

That's it! The database schema will be automatically applied on first startup.

## Database Configuration

- **Host:** localhost
- **Port:** 5432
- **Database:** portfolio_db
- **Username:** portfolio_user
- **Password:** portfolio_password

## Services

### PostgreSQL Database
- **Container:** portfolio_postgres
- **Image:** postgres:15
- **Port:** 5432
- Automatically applies `schema.sql` on first initialization

### Adminer (Database Admin UI)
- **Container:** portfolio_adminer  
- **Port:** 8080
- **URL:** http://localhost:8080
- Use the database credentials above to connect

## Database Schema

The `schema.sql` file creates:

### Tables
- **transactions** - All buy/sell transactions with commission tracking
- **positions** - Calculated positions based on transactions

### Views
- **portfolio_summary** - Overall portfolio metrics
- **position_details** - Position-level calculations with P&L

### Functions & Triggers
- **recalculate_position()** - Recalculates position data from transactions
- **Auto-triggers** - Automatically update positions when transactions change

## Common Operations

### Start Database (Normal)
```bash
cd persistence/database
docker-compose up -d
```

### Stop Database
```bash
cd persistence/database
docker-compose down
```

### Reset Database (Clean Slate)
```bash
cd persistence/database
# Stop containers and remove ALL data
docker-compose down -v

# Start fresh (schema will be auto-applied)
docker-compose up -d

# Wait a moment for initialization, then test
sleep 10
python test_db_setup.py
```

### View Logs
```bash
cd persistence/database
# View all logs
docker-compose logs

# Follow logs in real-time
docker-compose logs -f

# View only PostgreSQL logs
docker-compose logs postgres
```

### Check Container Status
```bash
cd persistence/database
docker-compose ps
```

## Testing Database Setup

Run the test script to verify everything is working:

```bash
cd persistence/database
python test_db_setup.py
```

This will:
- Test database connectivity
- Verify all tables and views exist
- Check that functions and triggers are properly installed
- Display sample database information

## Troubleshooting

### Database Won't Start
```bash
# Check container logs
docker-compose logs postgres

# Common issues:
# 1. Port 5432 already in use
# 2. Docker out of disk space
# 3. Permission issues with volumes
```

### Schema Not Applied
```bash
# Reset and restart (this will delete all data!)
docker-compose down -v
docker-compose up -d
```

### Connection Issues
1. Ensure containers are running: `docker-compose ps`
2. Check if ports are available: `netstat -an | grep 5432`
3. Verify environment variables in `docker-compose.yml`

### Performance Issues
```bash
# Check container resource usage
docker stats portfolio_postgres

# Check database connections
docker-compose exec postgres psql -U portfolio_user -d portfolio_db -c "SELECT * FROM pg_stat_activity;"
```

## Development Notes

### Adding New Schema Changes
1. Update `schema.sql` with your changes
2. Reset the database: `docker-compose down -v && docker-compose up -d`
3. Test your changes: `python test_db_setup.py`

### Backup & Restore
```bash
# Backup
docker-compose exec postgres pg_dump -U portfolio_user portfolio_db > backup.sql

# Restore  
docker-compose exec -T postgres psql -U portfolio_user -d portfolio_db < backup.sql
```

### Manual Schema Application (if needed)
```bash
# Apply schema manually to running database
docker-compose exec -T postgres psql -U portfolio_user -d portfolio_db < schema.sql
```

## File Structure

```
persistence/database/
├── README.md              # This documentation
├── docker-compose.yml     # Container orchestration
├── schema.sql            # Database schema definition
└── test_db_setup.py      # Database verification script
```

## Environment Variables

You can override default settings using environment variables:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=portfolio_db
export DB_USER=portfolio_user
export DB_PASSWORD=portfolio_password
```

## Security Notes

⚠️ **Important:** The default configuration uses simple credentials suitable for development only. For production:

1. Change default passwords
2. Use environment variables for sensitive data
3. Configure proper network security
4. Enable SSL/TLS connections
5. Restrict database user permissions

## Next Steps

After successful database setup:
1. Explore the connection utilities in `persistence/database/connection.py`
2. Check out the CRUD operations in `persistence/crud/portfolio_crud.py`
3. Run the demo scripts in `persistence/demo/`
4. Start building your portfolio tracking application! 