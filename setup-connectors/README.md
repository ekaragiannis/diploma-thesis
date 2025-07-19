# Kafka Connect Connector Configurations

This directory contains the configuration files and deployment scripts for Kafka Connect connectors used in the IoT data processing pipeline.

## Overview

The Kafka Connect service acts as a bridge between external systems and Kafka topics, enabling streaming data integration without custom code. This setup includes four main connectors that form the data pipeline:

```
MQTT → Kafka → TimescaleDB → Kafka → Redis
```

## Connector Configurations

The connector configurations are stored in `configs/` and deployed via the initialization script.

### 1. MQTT Source Connector (`source.mqtt.rawdata.json`)

**Purpose**: Consumes sensor data from MQTT broker and publishes to Kafka

**Key Configuration**:

- **Topic Pattern**: `/sensors/+` (wildcard for all sensor topics)
- **Output Topic**: `mqtt.rawdata`
- **Quality of Service**: 1 (at least once delivery)
- **Transformations**:
  - `extractKey`: Extracts topic name as message key
  - `includeField`: Filters to only include `energy` and `timestamp` fields
  - `cast`: Converts energy values to float64
  - `setSchemaMetadata`: Sets Avro schema name

**Data Flow**:

```
MQTT Topic: /sensors/sensor_001 → Kafka Topic: mqtt.rawdata
```

### 2. Database Sink Connector (`sink.db.rawdata.json`)

**Purpose**: Stores raw sensor data from Kafka into TimescaleDB

**Key Configuration**:

- **Input Topic**: `db.rawdata`
- **Target Table**: `rawdata`
- **Insert Mode**: `upsert` (insert or update on conflict)
- **Primary Keys**: `sensor`, `timestamp`
- **Transformations**:
  - `TimestampConverter`: Converts Unix timestamps to SQL timestamp format

**Data Flow**:

```
Kafka Topic: db.rawdata → TimescaleDB Table: rawdata
```

### 3. Database Source Connector (`source.db.hourlydata.json`)

**Purpose**: Captures changes from TimescaleDB hourly aggregation table using Change Data Capture (CDC)

**Key Configuration**:

- **Connector Type**: Debezium PostgreSQL
- **Output Topic Prefix**: `db`
- **Schema Monitoring**: `_timescaledb_internal` (TimescaleDB internal schema)
- **Plugin**: `pgoutput` (PostgreSQL logical replication)
- **Transformations**:
  - `timescaledb`: TimescaleDB-specific transform for hypertable handling
  - `includeField`: Extracts only the `after` field (new row state)
  - `setSchemaMetadata`: Sets Avro schema name

**Data Flow**:

```
TimescaleDB Table: hourlydata → Kafka Topic: db.public.hourlydata
```

### 4. Redis Sink Connector (`sink.redis.aggdata.json`)

**Purpose**: Caches aggregated data in Redis for fast API access

**Key Configuration**:

- **Input Topic**: `redis.aggdata`
- **Redis Command**: `JSONSET` (stores data as JSON documents)
- **Data Format**: JSON with Avro schema support

**Data Flow**:

```
Kafka Topic: redis.aggdata → Redis JSON documents
```

## Initialization Script

The `init-connectors.sh` script automatically deploys all connectors:

### Features

- **Automated Deployment**: Deploys all four connectors in sequence
- **Error Handling**: Gracefully handles existing connectors
- **Verification**: Lists deployed connectors for confirmation
- **Health Checks**: Verifies Kafka Connect API accessibility

### Usage

The script runs automatically as part of the Docker Compose startup sequence, but can also be executed manually:

```bash
docker compose up setup-connectors
```

## Environment Variables

Connectors use environment variables for configuration, loaded from `.env` file:

- `MQTT_HOST`: MQTT broker hostname and port
- `MQTT_USER`: MQTT authentication username
- `MQTT_PASSWORD`: MQTT authentication password
- `DB_CONNECTION_URL`: JDBC connection string for TimescaleDB
- `DB_HOST`: Database hostname
- `DB_PORT`: Database port
- `DB_NAME`: Database name
- `DB_USER`: Database username
- `DB_PASSWORD`: Database password
- `REDIS_URI`: Redis connection URI
- `KAFKA_SCHEMA_REGISTRY`: Schema Registry URL

## Monitoring

### Connector Status

Check connector status via Kafka Connect REST API:

```bash
curl http://localhost:8083/connectors
curl http://localhost:8083/connectors/source.mqtt.rawdata/status
```
