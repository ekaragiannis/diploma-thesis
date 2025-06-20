#!/bin/bash
set -e

KAFKA_BOOTSTRAP="broker:29092"

echo "Creating topics..."

echo "Creating topic: mqtt.SensorsMetrics"
if ! kafka-topics --bootstrap-server "$KAFKA_BOOTSTRAP" \
                 --create --if-not-exists \
                 --topic "mqtt.SensorsMetrics" \
                 --partitions "3" \
                 --replication-factor "1" \
                 --config retention.ms=3600000; then
  echo "ERROR: Failed to create topic mqtt.SensorsMetrics"
else
  echo "Kafka topic mqtt.SensorsMetrics created (or already exists)"
fi

echo "Creating topic: db.public.HourlySummary"
if ! kafka-topics --bootstrap-server "$KAFKA_BOOTSTRAP" \
                 --create --if-not-exists \
                 --topic "db.public.HourlySummary" \
                 --partitions "3" \
                 --replication-factor "1" \
                 --config retention.ms=90000000; then
  echo "ERROR: Failed to create topic db.public.HourlySummary"
else
  echo "Kafka topic db.public.HourlySummary created (or already exists)"
fi

echo "Creating topic: redis.HourlySummary"
if ! kafka-topics --bootstrap-server "$KAFKA_BOOTSTRAP" \
                 --create --if-not-exists \
                 --topic "redis.HourlySummary" \
                 --partitions "3" \
                 --replication-factor "1" \
                 --config cleanup.policy=compact; then
  echo "ERROR: Failed to create topic redis.HourlySummary"
else
  echo "Kafka topic redis.HourlySummary created (or already exists)"
fi

echo "Topics creation complete"