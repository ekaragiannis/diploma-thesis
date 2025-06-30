package com.example.kstreams;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    static final String SENSOR_INPUT_TOPIC = "db.public.HourlySummary";
    static final String SENSOR_OUTPUT_TOPIC = "redis.HourlySummary";

    // Reuse a single ObjectMapper instance
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // Greece Athens timezone
    private static final ZoneId GREECE_ATHENS_ZONE = ZoneId.of("Europe/Athens");

    public static void main(String[] args) {
        logger.info("Starting Kafka Streams application...");

        // Get environment variables
        String kafkaBootstrapServers = System.getenv().getOrDefault("KAKFA_BOOTSTRAP", "broker:29092");
        String schemaRegistryUrl = System.getenv().getOrDefault("KAFKA_SCHEMA_REGISTRY", "http://schema-registry:8081");

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.topicPrefix(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG), 1); // Set to replication factor -
                                                                                          // 1
        props.put(StreamsConfig.NUM_STANDBY_REPLICAS_CONFIG, 0); // Set to stream instances - 1
        props.put("schema.registry.url", schemaRegistryUrl);

        StreamsBuilder builder = new StreamsBuilder();

        // Configure serde
        Map<String, String> serdeConfig = Map.of("schema.registry.url", schemaRegistryUrl);

        final GenericAvroSerde valueSerde = new GenericAvroSerde();
        valueSerde.configure(serdeConfig, false);

        final Serde<String> keySerde = Serdes.String();

        try {
            KStream<String, GenericRecord> stream = builder.stream(SENSOR_INPUT_TOPIC,
                    Consumed.with(keySerde, valueSerde));

            // Convert directly to KTable without intermediate topic
            KTable<String, String> table = stream
                    .map((oldKey, value) -> {
                        logger.debug("Processing record with key: {}", oldKey);
                        if (value == null) {
                            logger.warn("Received null value for key: {}", oldKey);
                            return new KeyValue<>(null, null);
                        }

                        // Navigate into the nested record
                        GenericRecord after = (GenericRecord) value.get("after");
                        if (after == null) {
                            logger.warn("No 'after' field found in record");
                            return new KeyValue<>(null, null);
                        }

                        // Extract sensor from the nested record
                        Object sensorObj = after.get("sensor");
                        if (sensorObj == null) {
                            logger.warn("No 'sensor' field found in 'after' record");
                            return new KeyValue<>(null, null);
                        }
                        String sensor = sensorObj.toString();

                        // Extract hour from the nested record
                        Object hourBucketObj = after.get("hour_bucket");
                        if (hourBucketObj == null) {
                            logger.warn("No 'hour_bucket' field found in 'after' record");
                            return new KeyValue<>(null, null);
                        }
                        String hourTimestamp = hourBucketObj.toString();

                        // Extract value from the nested record
                        Object energyObj = after.get("energy_total");
                        if (energyObj == null) {
                            logger.warn("No 'energy_total' field found in 'after' record");
                            return new KeyValue<>(null, null);
                        }
                        Double energy = ((Number) energyObj).doubleValue();

                        // Parse the ISO timestamp and convert to Greece Athens timezone
                        String hourOfDay;
                        try {
                            OffsetDateTime dateTime = OffsetDateTime.parse(hourTimestamp,
                                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                            // Convert to Greece Athens timezone
                            ZonedDateTime greeceTime = dateTime.atZoneSameInstant(GREECE_ATHENS_ZONE);
                            hourOfDay = String.format("%02d", greeceTime.getHour()); // always 2 digits

                            logger.debug("Original timestamp: {}, Greece Athens time: {}, Hour: {}",
                                    hourTimestamp, greeceTime, hourOfDay);
                        } catch (Exception e) {
                            logger.error("Failed to parse timestamp: {}", hourTimestamp, e);
                            return new KeyValue<>(null, null);
                        }

                        String newKey = sensor + "|" + hourOfDay;
                        logger.debug("Created new key: {}", newKey);

                        ObjectNode newValue = OBJECT_MAPPER.createObjectNode();
                        newValue.put("hour_bucket", hourOfDay);
                        newValue.put("energy", energy);

                        String jsonString = newValue.toString();
                        return new KeyValue<>(newKey, jsonString);
                    })
                    .toTable(Materialized.with(Serdes.String(), Serdes.String()));

            KStream<String, String> jsonStream = table.toStream()
                    .map((key, value) -> {
                        if (key == null || value == null) {
                            return new KeyValue<>(null, null);
                        }

                        // Extract sensor from composite key
                        String[] parts = key.split("\\|");
                        if (parts.length != 2) {
                            logger.warn("Invalid key format: {}", key);
                            return new KeyValue<>(null, null);
                        }
                        String sensor = parts[0];

                        return new KeyValue<>(sensor, value);
                    });

            KTable<String, String> aggregated = jsonStream
                    .groupByKey()
                    .aggregate(
                            () -> "{}",
                            (sensor, json, aggregate) -> {
                                try {
                                    JsonNode newNode = OBJECT_MAPPER.readTree(json);
                                    String hourBucket = newNode.get("hour_bucket").asText();
                                    double energy = newNode.get("energy").asDouble();
                                    ObjectNode result = (ObjectNode) OBJECT_MAPPER.readTree(aggregate);
                                    result.put(hourBucket, energy);

                                    return OBJECT_MAPPER.writeValueAsString(result);
                                } catch (Exception e) {
                                    logger.error("Failed to aggregate JSON for sensor: {}", sensor, e);
                                    return aggregate;
                                }
                            },
                            Materialized.with(Serdes.String(), Serdes.String()));

            aggregated.toStream().to(SENSOR_OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

            KafkaStreams streams = new KafkaStreams(builder.build(), props);

            // Add exception handler
            streams.setUncaughtExceptionHandler(exception -> {
                logger.error("Uncaught exception in streams application", exception);
                return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
            });

            logger.info("Starting streams...");
            streams.start();

            logger.info("Streams started successfully. Application is running...");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down streams...");
                streams.close();
                logger.info("Streams shut down complete.");
            }));

        } catch (Exception e) {
            logger.error("Failed to start Kafka Streams application", e);
            System.exit(1);
        }
    }
}