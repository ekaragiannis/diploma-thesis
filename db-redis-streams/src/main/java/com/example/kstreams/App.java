package com.example.kstreams;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.avro.HourEnergy;
import com.example.avro.RedisAggData;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

/**
 * Database to Redis Kafka Streams Application
 * 
 * This application processes hourly aggregated sensor data from TimescaleDB and
 * caches it in Redis.
 * It serves as the final stage in the data pipeline, consuming CDC events from
 * the database
 * and producing optimized data structures for fast web API access.
 * 
 * Data Flow:
 * 1. Consumes hourly aggregated data from db.public.hourlydata topic (Debezium
 * CDC format)
 * 2. Extracts sensor data from CDC "after" field and converts timestamps
 * 3. Filters data to only include records from the last 24 hours
 * 4. Aggregates hourly data per sensor with automatic size limiting
 * 5. Produces Redis-ready data to redis.aggdata topic for caching
 * 
 * Key features:
 * - Time-windowed data filtering (last 24 hours only)
 * - Per-sensor aggregation with automatic data retention
 * - CDC record processing from Debezium connector
 * - Timestamp conversion from database microseconds to UTC strings
 */
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    /** Input: Hourly aggregated data from TimescaleDB via CDC */
    static final String SENSOR_INPUT_TOPIC = "db.public.hourlydata";
    /** Output: Aggregated data for Redis caching */
    static final String SENSOR_OUTPUT_TOPIC = "redis.aggdata";
    /** Maximum number of hourly records to keep per sensor (24 hours) */
    static final int MAX_HOURS_PER_SENSOR = 24;
    /** UTC timestamp formatter for database compatibility */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC);

    public static void main(String[] args) {
        logger.info("Starting Database to Redis Kafka Streams application...");

        // Load configuration from environment variables with fallback defaults
        String kafkaBootstrapServers = System.getenv().getOrDefault("KAKFA_BOOTSTRAP", "broker:29092");
        String schemaRegistryUrl = System.getenv().getOrDefault("KAFKA_SCHEMA_REGISTRY", "http://schema-registry:8081");

        // Initialize Kafka Streams configuration and topology builder
        Properties props = createStreamsProperties(kafkaBootstrapServers, schemaRegistryUrl);
        StreamsBuilder builder = new StreamsBuilder();

        // Configure Avro serialization/deserialization for Schema Registry integration
        Map<String, String> serdeConfig = Map.of("schema.registry.url", schemaRegistryUrl);

        // Serde for input messages (Generic Avro for Debezium CDC records)
        final GenericAvroSerde valueSerde = new GenericAvroSerde();
        valueSerde.configure(serdeConfig, false); // false = value serde

        // Serde for intermediate transformed data (HourEnergy schema)
        final SpecificAvroSerde<HourEnergy> transformedDataSerde = new SpecificAvroSerde<>();
        transformedDataSerde.configure(serdeConfig, false); // false = value serde

        // Serde for output messages (Redis aggregated data with RedisAggData schema)
        final SpecificAvroSerde<RedisAggData> redisValueSerde = new SpecificAvroSerde<>();
        redisValueSerde.configure(serdeConfig, false); // false = value serde

        final Serde<String> keySerde = Serdes.String();

        try {
            KStream<String, HourEnergy> transformedDataStream = builder.stream(SENSOR_INPUT_TOPIC,
                    Consumed.with(keySerde, valueSerde))
                    .map(energyDataMapper) // Transform CDC to HourEnergy format
                    .filter((key, value) -> key != null || value != null) // Remove invalid records
                    .filter(isWithinLast24HoursFilter); // Keep only last 24 hours

            // Aggregate hourly data per sensor with automatic size management
            KTable<String, RedisAggData> aggregated = transformedDataStream
                    .groupByKey(Grouped.with(keySerde, transformedDataSerde))
                    .aggregate(
                            () -> new RedisAggData(new ArrayList<HourEnergy>()), // Initialize empty aggregation
                            redisDataAggregator, // Aggregate function with size limiting
                            Materialized.with(Serdes.String(), redisValueSerde)); // Materialized state store

            aggregated.toStream().to(SENSOR_OUTPUT_TOPIC, Produced.with(keySerde, redisValueSerde));

            KafkaStreams streams = new KafkaStreams(builder.build(), props);

            // Configure global exception handling - shutdown client on uncaught exceptions
            streams.setUncaughtExceptionHandler(exception -> {
                logger.error("Uncaught exception in streams application", exception);
                return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
            });

            // Start the streams application
            logger.info("Starting Database to Redis streams...");
            streams.start();

            logger.info("Database to Redis streams started successfully. Application is running...");

            // Register shutdown hook for graceful application termination
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down Database to Redis streams...");
                streams.close();
                logger.info("Database to Redis streams shutdown complete.");
            }));

        } catch (Exception e) {
            logger.error("Error setting up streams topology", e);
            System.exit(1);
        }
    }

    /**
     * Creates and configures the Kafka Streams properties for the application.
     * 
     * @param kafkaBootstrapServers Comma-separated list of Kafka broker addresses
     * @param schemaRegistryUrl     URL of the Confluent Schema Registry service
     * @return Configured Properties object for Kafka Streams
     */
    private static Properties createStreamsProperties(String kafkaBootstrapServers, String schemaRegistryUrl) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "db-redis-streams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.topicPrefix(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG), 1);
        props.put(StreamsConfig.NUM_STANDBY_REPLICAS_CONFIG, 0);
        props.put("schema.registry.url", schemaRegistryUrl);
        return props;
    }

    /**
     * Aggregator function that combines hourly energy data per sensor.
     * 
     * This aggregator maintains a rolling window of the last 24 hours of data per
     * sensor:
     * - Adds new hourly energy records to the sensor's data list
     * - Updates existing records if they have the same hour bucket
     * - Automatically removes oldest records when exceeding the 24-hour limit
     * - Ensures data freshness and prevents unbounded memory growth
     * 
     * Input: sensor (key), new HourEnergy record (value), current RedisAggData
     * (aggregate)
     * Output: Updated RedisAggData with the new record incorporated
     */
    private static Aggregator<String, HourEnergy, RedisAggData> redisDataAggregator = (key, newValue, aggregate) -> {
        // Get the current list of hourly energy records for this sensor
        List<HourEnergy> hourEnergies = aggregate.getData();
        String hourBucket = newValue.getHourBucket().toString();
        Double energyTotal = newValue.getEnergyTotal();

        // Check if we already have a record for this hour bucket
        HourEnergy existingHourEnergy = hourEnergies.stream()
                .filter(hourEnergy -> hourEnergy.getHourBucket().toString().equals(hourBucket))
                .findFirst()
                .orElse(null);

        if (existingHourEnergy == null) {
            // Add new hourly record
            HourEnergy newHourEnergy = new HourEnergy();
            newHourEnergy.setEnergyTotal(energyTotal);
            newHourEnergy.setHourBucket(hourBucket);
            hourEnergies.add(newHourEnergy);

            // Enforce size limit by removing oldest records
            if (hourEnergies.size() > MAX_HOURS_PER_SENSOR) {
                HourEnergy oldest = hourEnergies.stream()
                        .min(Comparator.comparing(e -> Instant.parse(e.getHourBucket().toString())))
                        .orElse(null);
                if (oldest != null) {
                    hourEnergies.remove(oldest);
                }
            }
        } else {
            // Update existing record with new energy total
            existingHourEnergy.setEnergyTotal(energyTotal);
        }

        return aggregate;
    };

    /**
     * Converts a timestamp from database format to UTC string format.
     * 
     * TimescaleDB stores timestamps as microseconds since Unix epoch.
     * This method converts them to human-readable UTC strings for Redis storage.
     *
     * @param hourTimestamp The timestamp in microseconds since epoch
     * @return The UTC timestamp string (e.g., '2024-06-01 14:00:00'), or null if
     *         conversion fails
     */
    private static String convertTimestampToUtcString(Long hourTimestamp) {
        try {
            // Convert microseconds to seconds and nanoseconds
            Long secondsSinceEpoch = hourTimestamp / 1_000_000;
            Long nanosAdjustment = (hourTimestamp % 1_000_000) * 1000;

            // Create Instant and format as UTC string
            Instant instant = Instant.ofEpochSecond(secondsSinceEpoch, nanosAdjustment);
            return FORMATTER.format(instant);
        } catch (Exception e) {
            logger.error("Failed to parse timestamp: {}", hourTimestamp, e);
            return null;
        }
    }

    /**
     * KeyValue mapper that transforms Debezium CDC records to HourEnergy format.
     * 
     * This mapper processes Change Data Capture events from TimescaleDB:
     * - Input: (null, Debezium CDC GenericRecord)
     * - Output: (sensor, HourEnergy)
     * 
     * The transformation includes:
     * - Extracting the "after" field from CDC record (contains new row data)
     * - Converting database timestamp format to UTC string
     * - Creating HourEnergy record with sensor data
     * - Using sensor field as the new message key for partitioning
     */
    private static KeyValueMapper<String, GenericRecord, KeyValue<String, HourEnergy>> energyDataMapper = (key,
            value) -> {
        try {
            // Extract the "after" field containing the new row state from CDC record
            GenericRecord after = getNestedRecord(value, "after");
            if (after == null) {
                return new KeyValue<>(null, null); // Invalid CDC record, will be filtered out
            }

            // Extract fields from the hourly data record
            String sensor = getStringField(after, "sensor");
            Long hourBucket = getLongField(after, "hour_bucket"); // Timestamp in microseconds
            Double energyTotal = getDoubleField(after, "energy_total");

            // Validate that all required fields are present
            if (sensor == null || hourBucket == null || energyTotal == null) {
                logger.warn("Missing required fields in CDC record for key: {}", key);
                return new KeyValue<>(null, null); // Will be filtered out
            }

            // Convert database timestamp to human-readable UTC string
            String hourBucketTimestamp = convertTimestampToUtcString(hourBucket);
            if (hourBucketTimestamp == null) {
                return new KeyValue<>(null, null); // Timestamp conversion failed
            }

            HourEnergy energyData = new HourEnergy();
            energyData.setEnergyTotal(energyTotal);
            energyData.setHourBucket(hourBucketTimestamp);

            return KeyValue.pair(sensor, energyData);

        } catch (Exception e) {
            logger.error("Error processing CDC record for key: {}", key, e);
            return new KeyValue<>(null, null); // Return null to filter out invalid records
        }
    };

    /**
     * Safely extracts a nested GenericRecord from an Avro record.
     * 
     * @param record    The parent GenericRecord to extract from
     * @param fieldName The name of the nested field
     * @return The nested GenericRecord, or null if not found or wrong type
     */
    private static GenericRecord getNestedRecord(GenericRecord record, String fieldName) {
        Object nested = record.get(fieldName);
        if (!(nested instanceof GenericRecord)) {
            logger.warn("No '{}' field found or not a GenericRecord", fieldName);
            return null;
        }
        return (GenericRecord) nested;
    }

    /**
     * Safely extracts a string field from an Avro record.
     * 
     * @param record    The GenericRecord to extract from
     * @param fieldName The name of the string field
     * @return The string value, or null if field is missing
     */
    private static String getStringField(GenericRecord record, String fieldName) {
        Object value = record.get(fieldName);
        if (value == null) {
            logger.warn("No '{}' field found in record", fieldName);
            return null;
        }
        return value.toString();
    }

    /**
     * Safely extracts a long field from an Avro record.
     * 
     * @param record    The GenericRecord to extract from
     * @param fieldName The name of the numeric field
     * @return The long value, or null if field is missing or not a number
     */
    private static Long getLongField(GenericRecord record, String fieldName) {
        Object value = record.get(fieldName);
        if (!(value instanceof Number)) {
            logger.warn("No '{}' field found or not a number", fieldName);
            return null;
        }
        return ((Number) value).longValue();
    }

    /**
     * Safely extracts a double field from an Avro record.
     * 
     * @param record    The GenericRecord to extract from
     * @param fieldName The name of the numeric field
     * @return The double value, or null if field is missing or not a number
     */
    private static Double getDoubleField(GenericRecord record, String fieldName) {
        Object value = record.get(fieldName);
        if (!(value instanceof Number)) {
            logger.warn("No '{}' field found or not a number", fieldName);
            return null;
        }
        return ((Number) value).doubleValue();
    }

    /**
     * Predicate filter that keeps only data from the last 24 hours.
     * 
     * It validates the timestamp format and checks that the hour bucket
     * falls within the last 24 hours from the current time.
     * 
     * @param key   The sensor (not used in filtering logic)
     * @param value The HourEnergy record with timestamp to validate
     * @return true if the record is within the last 24 hours, false otherwise
     */
    private static final Predicate<String, HourEnergy> isWithinLast24HoursFilter = (key, value) -> {
        String hourBucket = value.getHourBucket().toString();
        try {
            Instant bucketTime = Instant.from(FORMATTER.parse(hourBucket));
            Instant now = Instant.now();

            // Check if timestamp is within the last 24 hours and not in the future
            boolean isValid = !bucketTime.isBefore(now.minus(Duration.ofHours(24))) && !bucketTime.isAfter(now);

            if (!isValid) {
                logger.debug("Filtering out hour_bucket {}: not within last 24 hours", hourBucket);
            }

            return isValid;
        } catch (DateTimeParseException e) {
            logger.warn("Invalid hour_bucket format: {}", hourBucket, e);
            return false; // Filter out records with invalid timestamps
        }
    };

}