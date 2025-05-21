#!/bin/bash
set -e


echo "Deploying connectors..."

# Array of connector config files
CONNECTORS=(
  "source.mqtt.SensorsMetrics"
  "sink.db.SensorsMetrics"
  "source.db.HourlySummary"
  "sink.redis.HourlySummary"
)

# Loop through connectors and deploy
for connector in "${CONNECTORS[@]}"; do
  echo "Deploying connector: ${connector}"
  
  if ! curl -s -X POST -H "Content-Type: application/json" \
       --data @"configs/${connector}.json" \
       "$KAFKA_CONNECT/connectors"; then
    echo "ERROR: Failed to deploy connector ${connector}"
  else
    echo "Successfully deployed connector: ${connector}"
  fi
done

echo "Connectors deployment completed."