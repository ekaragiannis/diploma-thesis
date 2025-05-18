#!/bin/bash
set -e

echo "Uploading Avro schemas..."

# Function to properly format Avro schema for Schema Registry
upload_avro_schema() {
  local subject=$1
  local schema_file="schemas/$subject.avsc"

  # Use jq to create the payload (auto-escapes JSON)
  payload=$(jq -n --arg schema "$(cat "$schema_file")" '{"schema": $schema}')

  echo "Payload: $payload"  

  curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data "$payload" \
    "$KAFKA_SCHEMA_REGISTRY/subjects/$subject-value/versions"

}

upload_avro_schema "$KAFKA_TOPIC_MQTT_SENSORS_METRICS"



echo "Schemas uploaded."