#!/bin/bash

KAFKA_BOOTSTRAP="broker:29092"

TOPIC1="mqtt.sensors.data"
PARTITIONS1=3
REPLICATION1=1

TOPIC2="timescaledb.public.agg_data"
PARTITIONS2=3
REPLICATION2=1

TOPIC3="redis.agg_data"
PARTITIONS3=3
REPLICATION3=1

echo "Create topics..."

# Create topic to consume raw sensor events from MQTT source connector (input data from MQTT)
kafka-topics --bootstrap-server "$KAFKA_BOOTSTRAP" --create --if-not-exists --topic "$TOPIC1" --partitions "$PARTITIONS1" --replication-factor "$REPLICATION1"
echo "Kafka topic $TOPIC1 created (or already exists)"

# Create topic to publish change events captured from TimescaleDB's hourly aggregated table (using Debezium source connector)
kafka-topics --bootstrap-server "$KAFKA_BOOTSTRAP" --create --if-not-exists --topic "$TOPIC2" --partitions "$PARTITIONS2" --replication-factor "$REPLICATION2"
echo "Kafka topic $TOPIC2 created (or already exists)"

# Create topic to consume processed aggregation results after Kafka Streams processing (output for Redis or other sinks)
kafka-topics --bootstrap-server "$KAFKA_BOOTSTRAP" --create --if-not-exists --topic "$TOPIC3" --partitions "$PARTITIONS3" --replication-factor "$REPLICATION3"
echo "Kafka topic $TOPIC3 created (or already exists)"

echo "Topics created."