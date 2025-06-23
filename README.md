## 📋 Prerequisites

- Docker and Docker Compose

## 🚀 Quick Start

### 1. Environment Setup

Copy the environment file and configure it:

```bash
cp example.env .env
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

### 3. Verify Services

Check if all services are running:

```bash
docker-compose ps
```

Access the web interfaces:
- **Kafka UI**: http://localhost:8080
- **RedisInsight**: http://localhost:8001

## 🔧 Configuration

### Kafka Connect Connectors

The system includes pre-configured connectors:

- **MQTT Source**: `source.mqtt.SensorsMetrics` - Ingests sensor data from MQTT
- **TimescaleDB Sink**: `sink.db.SensorsMetrics` - Stores raw sensor data
- **TimescaleDB Source**: `source.db.HourlySummary` - Reads aggregated data
- **Redis Sink**: `sink.redis.HourlySummary` - Stores processed analytics

### Kafka Streams Application

The Java application processes data streams:
- Reads from `db.public.HourlySummary` topic
- Aggregates data by sensor ID and hour
- Writes results to `redis.HourlySummary` topic

## 🧪 Testing

### Step 1: Install Mosquitto CLI Tools
```bash
sudo apt-get install mosquitto-clients
```

### Step 2: Publish Test Messages

```bash
mosquitto_pub -h localhost -p 1883 -u mqttuser -P mqttpassword -t "/sensors/s1" -m '{"id": "s1", "value": 23.5}'
```

### Step 3: View Results

**Note**: Results will be available in Redis after approximately 10 minutes due to the TimescaleDB continuous aggregate policy schedule. 

If you want to see results immediately, you can manually refresh the continuous aggregate:

```bash
# Connect to TimescaleDB
docker exec -it timescaledb psql -U postgres -d postgres

# Manually refresh the continuous aggregate
CALL refresh_continuous_aggregate('"HourlySummary"', NULL, NULL);
```

This command will refresh the continuous aggregate for the last hour, making the processed data immediately available in Redis.
