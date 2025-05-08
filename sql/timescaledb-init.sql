CREATE PUBLICATION dbz_publication FOR ALL TABLES WITH (publish = 'insert, update');

CREATE TABLE sensors_data (
    device_id INT NOT NULL,
    time TIMESTAMP NOT NULL,
    temperature FLOAT
);

SELECT create_hypertable('sensors_data', 'time');

CREATE MATERIALIZED VIEW hourly_data
WITH (timescaledb.continuous) AS
SELECT
    device_id,
    time_bucket('1 hour', time) AS bucket,
    AVG(temperature) AS avg_temp,
    MAX(temperature) AS max_temp,
    MIN(temperature) AS min_temp
FROM sensors_data
GROUP BY device_id, bucket;

SELECT add_continuous_aggregate_policy('hourly_data',
    start_offset => INTERVAL '3 hours',
    end_offset   => INTERVAL '15 minutes',
    schedule_interval => INTERVAL '10 minutes');

