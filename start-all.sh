#!/bin/bash
set -e

echo "Starting services..."
docker compose up -d --build broker connect schema-registry kafka-rest-proxy kafka-ui timescaledb redis mqtt adminer setup-topics setup-schemas setup-connectors

echo "Waiting for setup services to complete..."
# Wait for all setup containers to exit (meaning they are done)
for service in setup-topics setup-schemas setup-connectors; do
  echo "Waiting for $service to stop..."
  container_id=$(docker compose ps -q $service)
  if [ -n "$container_id" ]; then
    docker wait $container_id
  else
    echo "Container $service not found or already stopped"
  fi
done

echo "Setup complete. Starting Kafka Streams..."
docker compose up -d --build mqtt-db-streams 

echo "Kafka Streams started."
