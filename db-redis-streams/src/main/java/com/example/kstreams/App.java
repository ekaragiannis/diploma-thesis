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

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    static final String SENSOR_INPUT_TOPIC = "db.public.hourlydata";
    static final String SENSOR_OUTPUT_TOPIC = "redis.aggdata";
    static final int MAX_HOURS_PER_SENSOR = 24;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC);

    /**
     * Main entry point for the Kafka Streams application.
     * Sets up the stream topology for aggregating hourly energy data per sensor and
     * writing results to Redis.
     */
    public static void main(String[] args) {
        logger.info("Starting Kafka Streams application...");

        // Get environment variables
        String kafkaBootstrapServers = System.getenv().getOrDefault("KAKFA_BOOTSTRAP", "broker:29092");
        String schemaRegistryUrl = System.getenv().getOrDefault("KAFKA_SCHEMA_REGISTRY", "http://schema-registry:8081");

        Properties props = createStreamsProperties(kafkaBootstrapServers, schemaRegistryUrl);
        StreamsBuilder builder = new StreamsBuilder();

        // Configure serde
        Map<String, String> serdeConfig = Map.of("schema.registry.url", schemaRegistryUrl);

        final GenericAvroSerde valueSerde = new GenericAvroSerde();
        valueSerde.configure(serdeConfig, false);

        final SpecificAvroSerde<HourEnergy> transformedDataSerde = new SpecificAvroSerde<>();
        transformedDataSerde.configure(serdeConfig, false);

        final SpecificAvroSerde<RedisAggData> redisValueSerde = new SpecificAvroSerde<>();
        redisValueSerde.configure(serdeConfig, false);

        final Serde<String> keySerde = Serdes.String();

        try {
            KStream<String, HourEnergy> transformedDataStream = builder.stream(SENSOR_INPUT_TOPIC,
                    Consumed.with(keySerde, valueSerde))
                    .map(energyDataMapper)
                    .filter((key, value) -> key != null || value != null)
                    .filter(isWithinLast24HoursFilter);

            KTable<String, RedisAggData> aggregated = transformedDataStream
                    .groupByKey(Grouped.with(keySerde, transformedDataSerde))
                    .aggregate(() -> new RedisAggData(new ArrayList<HourEnergy>()),
                            redisDataAggregator,
                            Materialized.with(Serdes.String(), redisValueSerde));

            aggregated.toStream().to(SENSOR_OUTPUT_TOPIC, Produced.with(keySerde, redisValueSerde));

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

    /**
     * Creates and configures the Kafka Streams properties
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

    private static Aggregator<String, HourEnergy, RedisAggData> redisDataAggregator = (key, newValue, aggregate) -> {

        List<HourEnergy> hourEnergies = aggregate.getData();
        String hourBucket = newValue.getHourBucket().toString();
        Double energyTotal = newValue.getEnergyTotal();

        HourEnergy existingHourEnergy = hourEnergies.stream()
                .filter(hourEnergy -> hourEnergy.getHourBucket().toString().equals(hourBucket))
                .findFirst()
                .orElse(null);

        if (existingHourEnergy == null) {
            HourEnergy newHourEnergy = new HourEnergy();
            newHourEnergy.setEnergyTotal(energyTotal);
            newHourEnergy.setHourBucket(hourBucket);
            hourEnergies.add(newHourEnergy);

            // If size exceeds limit, remove the oldest by timestamp
            if (hourEnergies.size() > MAX_HOURS_PER_SENSOR) {
                HourEnergy oldest = hourEnergies.stream()
                        .min(Comparator.comparing(e -> Instant.parse(e.getHourBucket().toString())))
                        .orElse(null);
                if (oldest != null) {
                    hourEnergies.remove(oldest);
                }
            }
        } else {
            existingHourEnergy.setEnergyTotal(energyTotal);
        }

        return aggregate;
    };

    /**
     * Converts a timestamp (microseconds since epoch) to a UTC ISO-8601 string.
     *
     * @param hourTimestamp The timestamp in microseconds since epoch
     * @return The UTC timestamp string (e.g., '2024-06-01 14:00:00'), or null if
     *         conversion fails
     */
    private static String convertTimestampToUtcString(Long hourTimestamp) {
        try {
            Long secondsSinceEpoch = hourTimestamp / 1_000_000;
            Long nanosAdjustment = (hourTimestamp % 1_000_000) * 1000;
            Instant instant = Instant.ofEpochSecond(secondsSinceEpoch, nanosAdjustment);
            return FORMATTER.format(instant);
        } catch (Exception e) {
            logger.error("Failed to parse timestamp: {}", hourTimestamp, e);
            return null;
        }
    }

    private static KeyValueMapper<String, GenericRecord, KeyValue<String, HourEnergy>> energyDataMapper = (key,
            value) -> {
        GenericRecord after = getNestedRecord(value, "after");
        if (after == null) {
            return new KeyValue<>(null, null);
        }

        String sensor = getStringField(after, "sensor");
        Long hourBucket = getLongField(after, "hour_bucket");
        Double energyTotal = getDoubleField(after, "energy_total");

        if (sensor == null || hourBucket == null || energyTotal == null) {
            return new KeyValue<>(null, null);
        }

        String hourBucketTimestamp = convertTimestampToUtcString(hourBucket);

        HourEnergy energyData = new HourEnergy();
        energyData.setEnergyTotal(energyTotal);
        energyData.setHourBucket(hourBucketTimestamp);

        return KeyValue.pair(sensor, energyData);
    };

    private static GenericRecord getNestedRecord(GenericRecord record, String fieldName) {
        Object nested = record.get(fieldName);
        if (!(nested instanceof GenericRecord)) {
            logger.warn("No '{}' field found or not a GenericRecord", fieldName);
            return null;
        }
        return (GenericRecord) nested;
    }

    private static String getStringField(GenericRecord record, String fieldName) {
        Object value = record.get(fieldName);
        if (value == null) {
            logger.warn("No '{}' field found in record", fieldName);
            return null;
        }
        return value.toString();
    }

    private static Long getLongField(GenericRecord record, String fieldName) {
        Object value = record.get(fieldName);
        if (!(value instanceof Number)) {
            logger.warn("No '{}' field found or not a number", fieldName);
            return null;
        }
        return ((Number) value).longValue();
    }

    private static Double getDoubleField(GenericRecord record, String fieldName) {
        Object value = record.get(fieldName);
        if (!(value instanceof Number)) {
            logger.warn("No '{}' field found or not a number", fieldName);
            return null;
        }
        return ((Number) value).doubleValue();
    }

    private static final Predicate<String, HourEnergy> isWithinLast24HoursFilter = (key, value) -> {
        String hourBucket = value.getHourBucket().toString();
        try {
            Instant bucketTime = Instant.from(FORMATTER.parse(hourBucket));
            Instant now = Instant.now();

            boolean isValid = !bucketTime.isBefore(now.minus(Duration.ofHours(24))) && !bucketTime.isAfter(now);
            if (!isValid) {
                logger.info("Filtering out hour_bucket {}: not within last 24 hours", hourBucket);
            }

            return isValid;
        } catch (DateTimeParseException e) {
            logger.warn("Invalid hour_bucket format: {}", hourBucket, e);
            return false;
        }
    };

}