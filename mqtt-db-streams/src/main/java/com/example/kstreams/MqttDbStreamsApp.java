package com.example.kstreams;

import java.time.Instant;
import java.util.Map;
import java.util.Properties;

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
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.avro.DbRawData;
import com.example.avro.MqttRawData;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

/**
 * MQTT to Database Kafka Streams Application
 * 
 * This application processes raw sensor data from MQTT topics and transforms it
 * for database storage.
 * It serves as a bridge in the data pipeline, consuming messages from the
 * mqtt.rawdata topic
 * and producing processed records to the db.rawdata topic for database
 * ingestion.
 * 
 * Data Flow:
 * 1. Consumes MQTT sensor data from mqtt.rawdata topic (MqttRawData schema)
 * 2. Extracts sensor name from the message key (MQTT topic path)
 * 3. Transforms the data structure to database format (DbRawData schema)
 * 4. Produces processed records to db.rawdata topic for JDBC sink connector
 * 
 * Key transformations:
 * - Changes message key from MQTT topic path to sensor name
 * - Adds sensor field to message value for database insertion
 * - Maintains data integrity with exactly-once processing semantics
 */
public class MqttDbStreamsApp {
  private static final Logger logger = LoggerFactory.getLogger(MqttDbStreamsApp.class);

  /** Input: Raw MQTT sensor data */
  static final String MQTT_INPUT_TOPIC = "mqtt.rawdata";
  /** Output: Processed data for database */
  static final String DB_OUTPUT_TOPIC = "db.rawdata";

  public static void main(String[] args) {
    logger.info("Starting MQTT to Database Kafka Streams application...");

    // Load configuration from environment variables with fallback defaults
    String kafkaBootstrapServers = System.getenv().getOrDefault("KAKFA_BOOTSTRAP", "broker:29092");
    String schemaRegistryUrl = System.getenv().getOrDefault("KAFKA_SCHEMA_REGISTRY", "http://schema-registry:8081");

    // Initialize Kafka Streams configuration and topology builder
    Properties props = createStreamsProperties(kafkaBootstrapServers, schemaRegistryUrl);
    StreamsBuilder builder = new StreamsBuilder();

    // Configure Avro serialization/deserialization for Schema Registry integration
    Map<String, String> serdeConfig = Map.of("schema.registry.url", schemaRegistryUrl);

    // Serde for input messages (MQTT raw data with MqttRawData schema)
    final SpecificAvroSerde<MqttRawData> mqttValueSerde = new SpecificAvroSerde<>();
    mqttValueSerde.configure(serdeConfig, false); // false = value serde

    // Serde for output messages (Database raw data with DbRawData schema)
    final SpecificAvroSerde<DbRawData> dbValueSerde = new SpecificAvroSerde<>();
    dbValueSerde.configure(serdeConfig, false); // false = value serde

    final Serde<String> keySerde = Serdes.String();

    try {
      KStream<String, DbRawData> processedStream = builder.stream(MQTT_INPUT_TOPIC,
          Consumed.with(keySerde, mqttValueSerde))
          .map(dbRawKeyValueMapper) // Transform each record (extract sensor, restructure data)
          .filter((key, value) -> key != null); // Filter out invalid records

      processedStream.to(DB_OUTPUT_TOPIC, Produced.with(Serdes.String(), dbValueSerde));

      KafkaStreams streams = new KafkaStreams(builder.build(), props);

      // Configure global exception handling - shutdown client on uncaught exceptions
      streams.setUncaughtExceptionHandler(exception -> {
        logger.error("Uncaught exception in streams application", exception);
        return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
      });

      // Start the streams application
      logger.info("Starting MQTT to Database streams...");
      streams.start();

      logger.info("MQTT to Database streams started successfully. Application is running...");

      // Register shutdown hook for graceful application termination
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        logger.info("Shutting down MQTT to Database streams...");
        streams.close();
        logger.info("MQTT to Database streams shutdown complete.");
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
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "mqtt-db-streams");
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
   * Extracts sensor name from MQTT topic key.
   * 
   * MQTT topics follow the pattern: /sensors/{sensor_name}
   * This method extracts the sensor_name from the topic path.
   * 
   * @param key MQTT topic path (e.g., "/sensors/sensor_001")
   * @return Sensor identifier (e.g., "sensor_001") or null if extraction fails
   */
  private static String extractSensorFromKey(String key) {
    // Split the topic path and get the last segment (sensor ID)
    String[] keyParts = key.split("/");
    String sensor = keyParts[keyParts.length - 1];

    // Validate that we successfully extracted the sensor name
    if (sensor == null || sensor.isEmpty()) {
      logger.warn("No sensor found in key: {}", key);
      return null;
    }

    return sensor;
  }

  /**
   * KeyValue mapper that transforms MQTT records to database-ready format.
   * 
   * This mapper performs the core data transformation:
   * - Input: (MQTT topic path, MqttRawData)
   * - Output: (sensor_id, DbRawData)
   * 
   * The transformation includes:
   * - Extracting sensor name from the message key (MQTT topic)
   * - Converting MqttRawData schema to DbRawData schema
   * - Adding sensor field to the message value for database insertion
   * - Handling missing timestamps with current time fallback
   */
  private static KeyValueMapper<String, MqttRawData, KeyValue<String, DbRawData>> dbRawKeyValueMapper = (key,
      value) -> {
    try {
      String sensor = extractSensorFromKey(key);
      if (sensor == null) {
        return new KeyValue<>(null, null); // Will be filtered out
      }

      Double energy = value.getEnergy();

      Long timestamp = value.getTimestamp();
      if (timestamp == null) {
        logger.warn("No 'timestamp' field found in record for sensor: {}. Using current time.", sensor);
        timestamp = Instant.now().toEpochMilli(); // Use current UTC time as fallback
      }

      DbRawData dbRawData = new DbRawData();
      dbRawData.setSensor(sensor);
      dbRawData.setEnergy(energy);
      dbRawData.setTimestamp(timestamp);

      return new KeyValue<>(sensor, dbRawData);

    } catch (Exception e) {
      logger.error("Error processing record for key: {}", key, e);
      return new KeyValue<>(null, null); // Return null to filter out invalid records
    }
  };
}