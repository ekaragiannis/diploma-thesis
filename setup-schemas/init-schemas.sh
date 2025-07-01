#!/bin/sh
set -e

# Configuration
KAFKA_BOOTSTRAP="${KAFKA_BOOTSTRAP:-broker:29092}"
SCHEMA_REGISTRY="${SCHEMA_REGISTRY:-http://schema-registry:8081}"

# Function to post schema to Schema Registry
post_schema() {
  local schema_file="$1"
  local subject_name="$2"
  
  echo "Posting schema: $subject_name"
  
  if [ -f "$schema_file" ]; then
    schema_request=$(jq -n --arg schema "$(cat "$schema_file")" '{"schema": $schema}')
    
    if curl -s -f -X POST \
      -H "Content-Type: application/vnd.schemaregistry.v1+json" \
      --data "$schema_request" \
      "$SCHEMA_REGISTRY/subjects/$subject_name/versions" > /dev/null 2>&1; then
      echo "Successfully posted schema: $subject_name"
    else
      echo "Schema $subject_name already exists or failed to post"
    fi
  else
    echo "Schema file not found: $schema_file"
  fi
}

# Post schemas to Schema Registry
echo "=== Posting Schemas to Schema Registry ==="

# Post MQTT sensor data schema (raw sensor readings from MQTT)
# post_schema "/opt/kafka/schemas/mqtt.rawdata-value.avsc" "mqtt.rawdata-value"

# Post database sensor data schema (processed data for database storage)
post_schema "/opt/kafka/schemas/db.rawdata-value.avsc" "db.rawdata-value"

# Post Redis aggregated sensor data schema (hourly aggregations for Redis)
post_schema "/opt/kafka/schemas/redis.aggdata-value.avsc" "redis.aggdata-value"

# Post Debezium hourly data schema (CDC events from database)
# post_schema "/opt/kafka/schemas/db.public.hourlydata-value.avsc" "db.public.hourlydata-value"

echo "=== Schema posting completed ==="

# Verify schemas
echo "=== Verifying Schemas ==="
echo "Schemas:"
curl -s "$SCHEMA_REGISTRY/subjects" | jq -r '.[]' 2>/dev/null || echo "Could not list schemas"

echo "=== Schemas setup completed successfully ===" 