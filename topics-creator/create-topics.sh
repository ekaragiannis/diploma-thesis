#!/bin/bash
set -e

KAFKA_BOOTSTRAP="broker:29092"

PARTITIONS=3
REPLICATION=1

echo "Creating topics..."

# Array of topics to create
TOPICS=(
  "$KAFKA_TOPIC_MQTT_SENSORS_METRICS"
  "$KAFKA_TOPIC_DB_HOURLY_METRICS"
  "$KAFKA_TOPIC_REDIS_HOURLY_METRICS"
)

# Loop through topics and create them
for topic in "${TOPICS[@]}"; do
  echo "Creating topic: ${topic}"
  
  if ! kafka-topics --bootstrap-server "$KAFKA_BOOTSTRAP" \
                   --create --if-not-exists \
                   --topic "$topic" \
                   --partitions "$PARTITIONS" \
                   --replication-factor "$REPLICATION"; then
    echo "ERROR: Failed to create topic ${topic}"
  else
    echo "Kafka topic ${topic} created (or already exists)"
  fi
done

echo "Topics creation complete"