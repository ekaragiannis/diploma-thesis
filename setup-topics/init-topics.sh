#!/bin/sh
set -e

# Configuration
KAFKA_BOOTSTRAP="${KAFKA_BOOTSTRAP:-broker:29092}"

# Function to create topic using Confluent CLI
create_topic() {
  local topic_name="$1"
  local partitions="${2:-3}"
  local replication_factor="${3:-1}"
  local config="${4:-}"
  
  echo "Creating topic: $topic_name"
  
  # Build the confluent kafka topic create command
  local cmd="kafka-topics --create --topic $topic_name --bootstrap-server $KAFKA_BOOTSTRAP --partitions $partitions --replication-factor $replication_factor"
  
  # Add config if provided
  if [ -n "$config" ]; then
    cmd="$cmd --config $config"
  fi
  
  # Add --if-not-exists flag to avoid errors if topic already exists
  cmd="$cmd --if-not-exists"
  
  if eval "$cmd" > /dev/null 2>&1; then
    echo "Successfully created topic: $topic_name"
  else
    echo "Topic $topic_name already exists or failed to create"
  fi
}

# Create topics
echo "=== Creating Kafka Topics ==="

# Topic for MQTT sensor data
create_topic "mqtt.SensorsMetrics" 3 1 "retention.ms=3600000"

# Topic for database hourly summary
create_topic "db.public.HourlySummary" 3 1 "retention.ms=90000000"

# Topic for Redis hourly summary
create_topic "redis.HourlySummary" 3 1 "cleanup.policy=compact"

echo "=== Topics creation completed ==="

# Verify topics
echo "=== Verifying Topics ==="
echo "Topics:"
kafka-topics --list --bootstrap-server $KAFKA_BOOTSTRAP 2>/dev/null || echo "Could not list topics"

echo "=== Topics setup completed successfully ===" 