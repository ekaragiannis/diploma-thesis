  INSERT INTO rawdata (sensor, timestamp, energy)
  SELECT
      $1 as sensor,
      timestamp_series as timestamp,
      (random() * 10 + 5)::FLOAT as energy
  FROM generate_series(
      (NOW() - INTERVAL '1 year')::timestamp,
      NOW()::timestamp,
      INTERVAL '1 second'
  ) as timestamp_series
  ;
