#!/bin/bash

NUM_SENSORS=$1

for i in $(seq 1 $NUM_SENSORS); do
  docker run -d --rm --env-file .env --network diploma-thesis_default produce-messages sensor_$i
  echo "Started sensor_$i"
done
