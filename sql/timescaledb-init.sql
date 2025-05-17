-- Necessary configuration for debezium to work
CREATE PUBLICATION dbz_publication FOR ALL TABLES WITH (publish = 'insert, update');

CREATE TABLE "SensorsData" (
    "id" TEXT NOT NULL,
    "ts" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "value" FLOAT NOT NULL
);

SELECT create_hypertable('"SensorsData"', 'ts');

CREATE MATERIALIZED VIEW "AggregatedData"
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 hour', ts) AS bucket,
    id,
    SUM(value) AS avg_value
FROM
    "SensorsData"
GROUP BY
    bucket, id;


SELECT add_continuous_aggregate_policy('"AggregatedData"',
    start_offset => INTERVAL '3 hours',
    end_offset   => INTERVAL '15 minutes',
    schedule_interval => INTERVAL '10 minutes');
