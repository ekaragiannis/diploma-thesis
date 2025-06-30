CREATE TABLE rawdata (
    "sensor" TEXT NOT NULL,
    "timestamp" TIMESTAMP NOT NULL,
    "energy" FLOAT NOT NULL
);

SELECT create_hypertable('rawdata', 'timestamp');

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


SELECT add_continuous_aggregate_policy('hourlydata',
    start_offset => INTERVAL '1 hour',
    end_offset   => NULL,
    schedule_interval => INTERVAL '10 minutes');


-- Necessary configuration for debezium to work
CREATE PUBLICATION dbz_publication FOR ALL TABLES WITH (publish = 'insert, update');