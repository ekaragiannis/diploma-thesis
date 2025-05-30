-- Necessary configuration for debezium to work
CREATE PUBLICATION dbz_publication FOR ALL TABLES WITH (publish = 'insert, update');

CREATE TABLE "SensorsMetrics" (
    "id" TEXT NOT NULL,
    "ts" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "value" FLOAT NOT NULL
);

SELECT create_hypertable('"SensorsMetrics"', 'ts');

CREATE MATERIALIZED VIEW "HourlySummary"
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 hour', ts) AS hour,
    id,
    SUM(value) AS hour_total
FROM
    "SensorsMetrics"
GROUP BY
    hour, id;


SELECT add_continuous_aggregate_policy('"HourlySummary"',
    start_offset => INTERVAL '1 hour',
    end_offset   => NULL,
    schedule_interval => INTERVAL '10 minutes');
