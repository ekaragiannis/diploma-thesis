#!/bin/sh
set -e

# Configuration
KAFKA_BOOTSTRAP="${KAFKA_BOOTSTRAP:-broker:29092}"
KAFKA_CONNECT="${KAFKA_CONNECT:-http://connect:8083}"

# Function to deploy connector
deploy_connector() {
  local connector_config="$1"
  local connector_name="$2"
  
  echo "Deploying connector: $connector_name"
  
  if [ -f "$connector_config" ]; then
    if curl -s -X POST \
      -H "Content-Type: application/json" \
      --data @"$connector_config" \
      "$KAFKA_CONNECT/connectors" > /dev/null 2>&1; then
      echo "Successfully deployed connector: $connector_name"
    else
      echo "Connector $connector_name already exists or failed to deploy"
    fi
  else
    echo "Connector config file not found: $connector_config"
  fi
}

# Deploy connectors
echo "=== Deploying Kafka Connect Connectors ==="

# Deploy MQTT source connector
deploy_connector "/opt/kafka/configs/source.mqtt.SensorsMetrics.json" "source.mqtt.SensorsMetrics"

# Deploy database sink connector
deploy_connector "/opt/kafka/configs/sink.db.SensorsMetrics.json" "sink.db.SensorsMetrics"

# Deploy database source connector (Debezium)
deploy_connector "/opt/kafka/configs/source.db.HourlySummary.json" "source.db.HourlySummary"

# Deploy Redis sink connector
deploy_connector "/opt/kafka/configs/sink.redis.HourlySummary.json" "sink.redis.HourlySummary"

echo "=== Connectors deployment completed ==="

# Verify connectors
echo "=== Verifying Connectors ==="
echo "Connectors:"
curl -s "$KAFKA_CONNECT/connectors" | jq -r '.[]' 2>/dev/null || echo "Could not list connectors"

echo "=== Connectors setup completed successfully ===" 