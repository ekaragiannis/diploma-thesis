#!/bin/bash

# Check if sensor name is provided
if [ $# -eq 0 ]; then
    echo "Usage: $0 <sensor_name>"
    echo "Example: $0 sensor_001"
    exit 1
fi

SENSOR_NAME=$1

# Insert data for the specified sensor
docker exec timescaledb psql -U postgres -d postgres -c "
INSERT INTO rawdata (sensor, timestamp, energy)
SELECT
    '$SENSOR_NAME' as sensor,
    timestamp_series as timestamp,
    (random() * 10 + 5)::FLOAT as energy
FROM generate_series(
    (NOW() - INTERVAL '1 year')::timestamp,
    NOW()::timestamp,
    INTERVAL '1 second'
) as timestamp_series;
"

echo "Inserted one year of data for $SENSOR_NAME"

docker exec timescaledb psql -U postgres -d postgres -c "
CALL refresh_continuous_aggregate('public.hourlydata', NULL, NULL)
"

echo "Hourly data refreshed"