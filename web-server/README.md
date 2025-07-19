# Web Server API

FastAPI-based REST API server that provides sensor data access for the data processing pipeline. The server acts as a bridge between the data storage layer (TimescaleDB and Redis) and the web client dashboard.

## Overview

The web server exposes HTTP endpoints to:

- Retrieve lists of available sensors
- Fetch sensor data from multiple sources (raw database, hourly aggregates, Redis cache)
- Provide health checks for system monitoring
- Deliver performance metrics via execution time tracking

## Architecture

### Framework & Dependencies

- **FastAPI**: Modern, fast web framework for building APIs with Python
- **Uvicorn**: ASGI server for running the FastAPI application
- **Pydantic**: Data validation and serialization using Python type annotations
- **psycopg2-binary**: PostgreSQL adapter for Python (TimescaleDB connection)
- **redis**: Python client for Redis cache access

### Project Structure

```
web-server/
├── main.py                 # Application entry point and configuration
├── requirements.txt        # Python dependencies
├── Dockerfile             # Container build configuration
└── app/
    ├── models/
    │   └── models.py       # Pydantic data models for API responses
    ├── routers/
    │   ├── sensor.py       # Sensor list endpoints
    │   └── sensor_data.py  # Sensor data retrieval endpoints
    ├── services/
    │   ├── db_service.py   # TimescaleDB connection and queries
    │   └── redis_service.py # Redis cache operations
    ├── middlewares/
    │   └── middleware.py   # Custom middleware (execution time tracking)
    ├── utils/
    │   ├── response_utils.py # Data formatting utilities
    │   └── time_utils.py   # Time/date manipulation helpers
    └── exceptions.py       # Global exception handlers
```

## Environment Configuration

The server reads configuration from environment variables:

```bash
# Database connection
DB_HOST=timescaledb
DB_PORT=5432
DB_NAME=postgres
DB_USER=postgres
DB_PASSWORD=password

# Redis connection
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_DB=0
```

## API Documentation

When the server is running, interactive API documentation is available at:

- **Swagger UI**: http://localhost:5000/docs
- **ReDoc**: http://localhost:5000/redoc
