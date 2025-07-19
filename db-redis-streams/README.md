# DB-Redis Streams Application

A Kafka Streams application that processes hourly aggregated sensor data from TimescaleDB and caches it in Redis. This service acts as the final stage in the data pipeline, optimizing data access for web APIs.

## Purpose

The application transforms database change events into Redis-optimized data structures:

- **Input**: Hourly aggregated data from TimescaleDB via CDC events
- **Output**: Time-windowed sensor data aggregated for Redis caching

## Data Flow

```
TimescaleDB Table: hourlydata � Kafka Topic: db.public.hourlydata
                                           �
                               [DB-Redis Streams Processing]
                                           �
Kafka Topic: redis.aggdata � Redis JSON documents
```

## Key Transformations

1. **CDC Processing**: Extracts "after" field from Debezium change events
2. **Timestamp Conversion**: Converts database microseconds to UTC strings
3. **Time Filtering**: Keeps only data from the last 24 hours
4. **Data Aggregation**: Groups hourly data per sensor with automatic size limiting
5. **Schema Conversion**: Transforms CDC format to `RedisAggData` schema

## Configuration

The application reads configuration from environment variables:

- `KAKFA_BOOTSTRAP`: Kafka broker addresses (default: `broker:29092`)
- `KAFKA_SCHEMA_REGISTRY`: Schema Registry URL (default: `http://schema-registry:8081`)

## Deployment

This application runs as a containerized service in the Docker Compose environment. It automatically starts when the IoT pipeline is launched and processes data continuously until the system is shut down.
