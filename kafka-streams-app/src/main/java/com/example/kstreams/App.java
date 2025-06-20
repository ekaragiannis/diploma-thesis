package com.example.kstreams;

import java.time.OffsetDateTime;
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

                        // Extract id from the nested record
                        Object idObj = after.get("id");
                        if (idObj == null) {
                            logger.warn("No 'id' field found in 'after' record");
                            return new KeyValue<>(null, null);
                        }
                        String id = idObj.toString();

                        // Extract hour from the nested record
                        Object hourObj = after.get("hour");
                        if (hourObj == null) {
                            logger.warn("No 'hour' field found in 'after' record");
                            return new KeyValue<>(null, null);
                        }
                        String hourTimestamp = hourObj.toString();

                        // Extract value from the nested record
                        Object valueObj = after.get("hour_total");
                        if (valueObj == null) {
                            logger.warn("No 'hour_total' field found in 'after' record");
                            return new KeyValue<>(null, null);
                        }
                        Double energy = ((Number) valueObj).doubleValue();

                        // Parse the ISO timestamp to extract only the hour (HH)
                        String hourOfDay;
                        try {
                            OffsetDateTime dateTime = OffsetDateTime.parse(hourTimestamp,
                                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                            hourOfDay = String.format("%02d", dateTime.getHour()); // always 2 digits
                        } catch (Exception e) {
                            logger.error("Failed to parse timestamp: {}", hourTimestamp, e);
                            return new KeyValue<>(null, null);
                        }

                        String newKey = id + "|" + hourOfDay;
                        logger.debug("Created new key: {}", newKey);

                        ObjectNode newValue = OBJECT_MAPPER.createObjectNode();
                        newValue.put("hour", hourOfDay);
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

                        // Extract id from composite key
                        String[] parts = key.split("\\|");
                        if (parts.length != 2) {
                            logger.warn("Invalid key format: {}", key);
                            return new KeyValue<>(null, null);
                        }
                        String id = parts[0];

                        return new KeyValue<>(id, value);
                    });

            KTable<String, String> aggregated = jsonStream
                    .groupByKey()
                    .aggregate(
                            () -> "{}",
                            (id, json, aggregate) -> {
                                try {
                                    JsonNode newNode = OBJECT_MAPPER.readTree(json);
                                    String hour = newNode.get("hour").asText();
                                    double energy = newNode.get("energy").asDouble();
                                    ObjectNode result = (ObjectNode) OBJECT_MAPPER.readTree(aggregate);
                                    result.put(hour, energy);

                                    return OBJECT_MAPPER.writeValueAsString(result);
                                } catch (Exception e) {
                                    logger.error("Failed to aggregate JSON for id: {}", id, e);
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