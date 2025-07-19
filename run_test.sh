#!/bin/bash

# Check if at least one sensor name is provided
if [ $# -eq 0 ]; then
    echo "Usage: $0 <sensor_name1> [sensor_name2] [sensor_name3] ..."
    echo "Example: $0 sensor_001 sensor_002 sensor_003"
    exit 1
fi

# Loop through all provided sensor names
for sensor_name in "$@"; do
    docker run -d --rm --env-file .env --network diploma-thesis_default produce-messages "$sensor_name"
    echo "Started $sensor_name"
done
