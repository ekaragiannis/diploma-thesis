-- TimescaleDB Initialization Script
-- This script sets up the database schema and configuration for the data processing pipeline.
-- It is executed automatically when the TimescaleDB container starts for the first time.

-- Table to store raw sensor data received from MQTT.
-- Columns:
--   sensor    - The sensor identifier.
--   timestamp - The time of data capture.
--   energy    - The energy reading from the sensor.
CREATE TABLE rawdata (
    "sensor" TEXT NOT NULL,
    "timestamp" TIMESTAMP NOT NULL,
    "energy" FLOAT NOT NULL
);

-- Create a unique index on (sensor, timestamp) to act as a composite primary key.
-- This is required to enable UPSERT mode in the JDBC sink connector.
CREATE UNIQUE INDEX rawdata_pk ON rawdata("sensor", "timestamp");

-- Convert the 'rawdata' table into a TimescaleDB hypertable.
-- This enables the use of TimescaleDB features like retention and continuous aggregates.
SELECT create_hypertable('rawdata', 'timestamp');

-- Add a retention policy to automatically remove rows older than 25 hours.
-- This prevents unbounded growth of the 'rawdata' hypertable.
SELECT add_retention_policy('rawdata', INTERVAL '25 hours');

-- Create a continuous aggregate materialized view for hourly energy data.
-- Aggregates energy readings per sensor, grouped into 1-hour buckets.
CREATE MATERIALIZED VIEW hourlydata
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 hour', timestamp) AS hour_bucket,
    sensor,
    SUM(energy) AS energy_total
FROM
    rawdata
GROUP BY
    hour_bucket, sensor;    

-- Add a continuous aggregate policy to keep the hourlydata view updated.
-- This policy recalculates the aggregate every 10 minutes, covering the past day.
SELECT add_continuous_aggregate_policy('hourlydata',
    start_offset => INTERVAL '1 day',
    end_offset   => NULL,
    schedule_interval => INTERVAL '10 minutes');

-- Create a publication for Debezium.
-- This is necessary for Debezium CDC (Change Data Capture) to monitor all tables.
CREATE PUBLICATION dbz_publication FOR ALL TABLES WITH (publish = 'insert, update');
