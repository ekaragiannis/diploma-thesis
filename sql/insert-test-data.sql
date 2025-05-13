INSERT INTO sensors_data (device_id, time, temperature)
SELECT
    FLOOR(RANDOM() * 3 + 1)::INT AS device_id,
    DATE_TRUNC('day', NOW()) - INTERVAL '2 day' * RANDOM() AS time,
    ROUND((RANDOM() * 43)::numeric, 2)::float AS temperature
FROM generate_series(1, 3000);