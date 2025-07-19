# Kafka Connect Runtime

This directory contains the Dockerfile for building a custom Kafka Connect runtime with all required connector plugins for the data processing pipeline.

## Dockerfile

The `Dockerfile` builds a custom Kafka Connect image with the following components:

### Base Image

- **confluentinc/cp-kafka-connect:8.0.0**: Official Confluent Kafka Connect runtime

### Installed Connectors

#### 1. JDBC Connector (`confluentinc/kafka-connect-jdbc:10.8.4`)

- Enables bidirectional data transfer between Kafka and SQL databases
- Used for both sink operations with TimescaleDB

#### 2. Redis Connector (`redis/redis-kafka-connect:0.9.1`)

- Streams data from Kafka topics to Redis
- Supports JSON storage for caching aggregated data

#### 3. Debezium PostgreSQL Connector (`debezium/debezium-connector-postgresql:3.1.2`)

- Captures change data from PostgreSQL/TimescaleDB using logical replication
- Streams database changes to Kafka topics in real-time

#### 4. Lenses MQTT Connector (`kafka-connect-mqtt-9.0.0`)

- Consumes messages from MQTT brokers
- Converts MQTT messages to Kafka records with configurable transformations

### Build Process

The Dockerfile uses a multi-stage build:

1. **Downloader Stage**: Downloads and extracts the Lenses MQTT connector
2. **Final Stage**: Installs Confluent Hub connectors and copies the MQTT connector

### Plugin Path Configuration

The `CONNECT_PLUGIN_PATH` environment variable includes:

- `/usr/share/java`: Default Java libraries
- `/usr/share/confluent-hub-components`: Confluent Hub installed connectors
- `/kafka-connect-plugins`: Custom connectors (MQTT)

### Environment Configuration

The container copies the `.env` file to `/secrets/properties` for secure access to environment variables used by connector configurations.

## Usage

This image is used by the `connect` service in the Docker Compose setup and provides the runtime environment for all Kafka Connect connectors in the data pipeline.
