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

### 3. Start Producing Messages

To start the data pipeline, you need to produce sensor messages. Use the provided Python script to simulate sensor data:

```bash
# Navigate to the produce-messages directory
cd produce-messages

# Activate virtual environment
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Start producing messages for a sensor (replace 'sensor_001' with your sensor name)
python produce_message.py sensor_001
```

**Multiple Sensors**: You can simulate multiple sensors by running the script with different sensor names in separate terminals:

```bash
# Terminal 1
python produce_message.py sensor_001

# Terminal 2
python produce_message.py sensor_002

# Terminal 3
python produce_message.py sensor_003
```

The script will:

- Connect to the MQTT broker automatically
- Generate random sensor data every second
- Publish messages to the topic `/sensors/{sensor_name}`
- Continue running until you press Ctrl+C

The system will automatically:

- Process incoming MQTT messages through Kafka Streams
- Store raw data in TimescaleDB
- Aggregate hourly data and cache it in Redis
- Make data available through the web dashboard

### 4. Web Interfaces

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
- **Stream Processing**: Simplify `db-redis-streams` logic and improve cached data format for better performance
- **API Enhancements**: Update `web-server` API responses with improved data structures and error handling
- **Component Library**: Integrate a modern UI component library for `web-client` to enhance responsiveness and user experience
- **Documentation**: Add a `README.md` file for each service, explaining its implementation and purpose
- **Kubernetes**: Migrate to Kubernetes for better scalability and container orchestration
