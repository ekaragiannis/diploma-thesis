#!/bin/bash
set -e

SCHEMA_REGISTRY_URL="http://schema-registry:8081"

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
    "$SCHEMA_REGISTRY_URL/subjects/$subject-value/versions"

}

upload_avro_schema "mqtt.sensors.data"



echo "Schemas uploaded."