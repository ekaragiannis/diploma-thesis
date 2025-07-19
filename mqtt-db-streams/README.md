# MQTT-DB Streams Application

A Kafka Streams application that processes raw sensor data from MQTT topics and transforms it for database storage. This service acts as a bridge in the data pipeline, enabling data flow from MQTT brokers to TimescaleDB.

## Purpose

The application transforms sensor data between two different schemas and message key formats:

- **Input**: Raw MQTT sensor messages with topic paths as keys
- **Output**: Database-ready records with sensor IDs as keys

This transformation is essential for the JDBC sink connector to properly insert sensor data into the TimescaleDB `rawdata` table.

## Data Flow

```
MQTT Topic: /sensors/sensor_001 → Kafka Topic: mqtt.rawdata
                                           ↓
                               [MQTT-DB Streams Processing]
                                           ↓
Kafka Topic: db.rawdata → TimescaleDB Table: rawdata
```

## Key Transformations

1. **Message Key**: Changes from MQTT topic path (`/sensors/sensor_001`) to sensor ID (`sensor_001`)
2. **Schema Conversion**: Transforms `MqttRawData` schema to `DbRawData` schema
3. **Data Enrichment**: Adds sensor field to message value for database primary key
4. **Error Handling**: Provides timestamp fallback and filters invalid records

## Technology Stack

- **Kafka Streams**: Stream processing framework
- **Apache Avro**: Schema-based data serialization
- **Confluent Schema Registry**: Centralized schema management
- **SLF4J**: Structured logging for monitoring and debugging

## Configuration

The application reads configuration from environment variables:

- `KAKFA_BOOTSTRAP`: Kafka broker addresses (default: `broker:29092`)
- `KAFKA_SCHEMA_REGISTRY`: Schema Registry URL (default: `http://schema-registry:8081`)

## Deployment

This application runs as a containerized service in the Docker Compose environment. It automatically starts when the IoT pipeline is launched and processes data continuously until the system is shut down.
