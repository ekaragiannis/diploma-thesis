## 📋 Prerequisites

- Docker and Docker Compose

## 🚀 Quick Start

### 1. Environment Setup

Copy the environment file and configure it:

```bash
cp .env.example .env
```

### 2. Start the Infrastructure

```bash
docker-compose up -d --build
```

This will start all services:

- Kafka broker on port 9092
- Schema Registry on port 8081
- Kafka Connect on port 8083
- Kafka REST Proxy on port 8082
- Kafka UI on port 8080
- TimescaleDB on port 5432
- Redis on port 6379 (RedisInsight on port 8001)
- MQTT broker on port 1883
- Web Server API on port 5000
- Web Client on port 80
- Adminer on port 8084
- DB-Redis Streams application (Java)
- MQTT-DB Streams application (Java)

### 3. Data Pipeline Architecture

The system processes sensor data through the following pipeline:

1. **MQTT → Kafka**: Sensor data flows from MQTT broker to `mqtt.rawdata` topic
2. **Kafka Streams Processing**: Raw data is processed and routed to `db.rawdata` topic
3. **Kafka → TimescaleDB**: Raw data is stored in `rawdata` table
4. **TimescaleDB Aggregation**: Hourly aggregation creates `hourlydata` table
5. **CDC Capture**: Debezium captures changes from `hourlydata` table
6. **Kafka Streams Processing**: Hourly data is processed and sent to `redis.aggdata` topic
7. **Kafka → Redis**: Aggregated data is cached in Redis for API access

### 4. Start Producing Messages

To activate the data pipeline, you'll need to begin producing sensor messages. Follow these steps from the project root directory:

#### Initial Setup

```bash
# Build the produce-messages container
docker build -t produce-messages produce-messages/

# Make scripts executable
chmod +x run_test.sh stop_test.sh

# Start producing messages to MQTT broker (requires sensor names)
./run_test.sh sensor_001
```

#### Multiple Sensors Simulation

You can simulate multiple sensors by specifying sensor names as arguments:

```bash
# Example: Start producing messages for specific sensors
./run_test.sh sensor_001 sensor_002 sensor_003

# Or start a single sensor
./run_test.sh sensor_001
```

#### What the Script Does

The message producer will:

- Automatically connect to the MQTT broker running on port 1883
- Generate realistic random sensor data every second
- Publish messages to topic pattern `/sensors/{sensor_name}`
- Continue running indefinitely until manually stopped

#### Stopping Message Production

To halt message production:

```bash
./stop_test.sh
```

The system will automatically:

- Process incoming MQTT messages through Kafka Streams
- Store raw data in TimescaleDB
- Aggregate hourly data and cache it in Redis
- Make data available through the web dashboard

### 5. Generate Historical Test Data

For testing and demonstration purposes, you can generate one year of historical data for any sensor:

```bash
# Make the script executable
chmod +x insert-one-year-data.sh

# Insert one year of historical data for a specific sensor
./insert-one-year-data.sh sensor_001
```

This script will:

- Insert data points for every second over the past year
- Generate random energy values between 5-15 units
- Automatically refresh the hourly aggregated data
- Provide a complete dataset for dashboard visualization

### 6. Web Interfaces

- **Kafka UI**: Web-based interface for monitoring Kafka topics, schemas, connectors, and cluster health. Access at http://localhost:8080
- **Web Client**: Interactive dashboard for visualizing sensor data from the last 24 hours with performance metrics. Access at http://localhost
- **RedisInsight**: Redis database management interface for viewing cached data and monitoring Redis performance. Access at http://localhost:8001
- **Adminer**: Database administration tool for managing TimescaleDB data and executing SQL queries. Access at http://localhost:8084
- **API Documentation**: Interactive Swagger UI for exploring and testing the web-server API endpoints. Access at http://localhost:5000/docs
- **API ReDoc**: Alternative API documentation interface with a more readable format. Access at http://localhost:5000/redoc

## 🔮 Planned Changes

This section outlines upcoming integrations and improvements for the IoT data processing system:

- **Prometheus & Grafana**: Integrate comprehensive monitoring and visualization for system metrics
- **Data Retention Policy**: Implement automatic cleanup for `rawdata` TimescaleDB table (drop data older than 1 day)
- **Documentation**: Add a `README.md` file for each service, explaining its implementation and purpose
- **Kubernetes**: Migrate to Kubernetes for better scalability and container orchestration
