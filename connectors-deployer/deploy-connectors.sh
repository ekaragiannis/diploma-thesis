#!/bin/bash
set -e

CONNECT_URL="http://connect:8083"
CONFIGS_DIR="configs"

CONNECTOR1="sink.timescaledb.SensorsData"
CONNECTOR2="source.timescaledb.AggregatedData"
CONNECTOR3="sink.redis.AggregatedData"

echo "Deploying connectors..."

curl -X POST -H "Content-Type: application/json" \
  --data @$CONFIGS_DIR/$CONNECTOR1.json \
  "$CONNECT_URL/connectors"

curl -X POST -H "Content-Type: application/json" \
  --data @$CONFIGS_DIR/$CONNECTOR2.json \
  "$CONNECT_URL/connectors"


curl -X POST -H "Content-Type: application/json" \
  --data @$CONFIGS_DIR/$CONNECTOR3.json \
  "$CONNECT_URL/connectors"

echo "Connectors deployed."
