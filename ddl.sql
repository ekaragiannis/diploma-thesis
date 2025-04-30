CREATE PUBLICATION dbz_publication FOR ALL TABLES WITH (publish = 'insert, update');

CREATE TABLE sensors_data (
    device_id INT NOT NULL,
    time TIMESTAMPTZ NOT NULL,
    temperature FLOAT,
    PRIMARY KEY (device_id, time)
);

SELECT create_hypertable('sensors_data', 'time');
