#!/bin/sh
set -e

INPUT_TOPIC="db.public.HourlySummary"

echo "Waiting for schema for topic: $INPUT_TOPIC..."
echo "$KAFKA_SCHEMA_REGISTRY/subjects/${INPUT_TOPIC}-value/versions/latest"

# Wait for the value schema to be registered
until curl -s "$KAFKA_SCHEMA_REGISTRY/subjects/${INPUT_TOPIC}-value/versions/latest" > /dev/null; do
  echo "Schema not yet available. Retrying..."
  sleep 10
done

echo "Creating ksqlDB streams..."

curl -X POST "$KSQL_SERVER/ksql" \
     -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
     -d @"-"<<EOF
{
  "ksql": "CREATE STREAM DbHourlySummary WITH ( KAFKA_TOPIC='db.public.HourlySummary', VALUE_FORMAT='AVRO' );",
  "streamsProperties": {}
}
EOF

curl -X POST "$KSQL_SERVER/ksql" \
     -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
     -d @"-"<<EOF
{ 
  "ksql": "CREATE TABLE RedisHourlySummary WITH ( kafka_topic='redis.HourlySummary', value_format='AVRO' ) AS SELECT after->id AS id, AS_MAP( COLLECT_LIST( FORMAT_TIMESTAMP( PARSE_TIMESTAMP(after->hour, 'yyyy-MM-dd''T''HH:mm:ss.SSSSSS''Z'''), 'HH' ) ), COLLECT_LIST(after->hour_total) ) AS \"data\" FROM DbHourlySummary WINDOW TUMBLING (SIZE 1 DAY) GROUP BY after->id EMIT CHANGES;",
  "streamsProperties": {}
}
EOF
