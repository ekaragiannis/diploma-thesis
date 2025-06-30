package com.example.kstreams;

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
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.avro.DbRawData;
import com.example.avro.MqttRawData;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

public class MqttDbStreamsApp {
  private static final Logger logger = LoggerFactory.getLogger(MqttDbStreamsApp.class);

  static final String MQTT_INPUT_TOPIC = "mqtt.rawdata";
  static final String DB_OUTPUT_TOPIC = "db.rawdata";

  public static void main(String[] args) {
    logger.info("Starting MQTT to Database Kafka Streams application...");

    // Get environment variables
    String kafkaBootstrapServers = System.getenv().getOrDefault("KAKFA_BOOTSTRAP", "broker:29092");
    String schemaRegistryUrl = System.getenv().getOrDefault("KAFKA_SCHEMA_REGISTRY", "http://schema-registry:8081");

    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "mqtt-db-streams-app");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
    props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    props.put(StreamsConfig.topicPrefix(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG), 1);
    props.put(StreamsConfig.NUM_STANDBY_REPLICAS_CONFIG, 0);
    props.put("schema.registry.url", schemaRegistryUrl);

    StreamsBuilder builder = new StreamsBuilder();

    // Configure serde
    Map<String, String> serdeConfig = Map.of("schema.registry.url", schemaRegistryUrl);

    final SpecificAvroSerde<MqttRawData> mqttValueSerde = new SpecificAvroSerde<>();
    mqttValueSerde.configure(serdeConfig, false);

    final SpecificAvroSerde<DbRawData> dbValueSerde = new SpecificAvroSerde<>();
    dbValueSerde.configure(serdeConfig, false);

    final Serde<String> keySerde = Serdes.String();

    try {
      KStream<String, MqttRawData> stream = builder.stream(MQTT_INPUT_TOPIC,
          Consumed.with(keySerde, mqttValueSerde));

      KStream<String, DbRawData> processedStream = stream
          .map((key, value) -> {
            logger.debug("Processing MQTT record with key: {}", key);
            if (value == null) {
              logger.warn("Received null value for key: {}", key);
              return new KeyValue<String, DbRawData>(null, null);
            }

            try {
              // Extract sensor ID from the topic key
              String[] keyParts = key.split("/");
              String sensor = keyParts[keyParts.length - 1];
              if (sensor == null || sensor.isEmpty()) {
                logger.warn("No sensor ID found in key");
                return new KeyValue<String, DbRawData>(null, null);
              }

              // Extract energy value from the Avro record
              Double energy = value.getEnergy();
              if (energy == null) {
                logger.warn("No 'energy' field found in record for sensor: {}", sensor);
                return new KeyValue<String, DbRawData>(null, null);
              }

              // Extract timestamp from the Avro record
              Long timestamp = value.getTimestamp();
              if (timestamp == null) {
                logger.warn("No 'timestamp' field found in record for sensor: {}", sensor);
                return new KeyValue<String, DbRawData>(null, null);
              }

              DbRawData dbRawData = new DbRawData(sensor, energy, timestamp);

              return new KeyValue<String, DbRawData>(sensor, dbRawData);
            } catch (Exception e) {
              logger.error("Error processing record for key: {}", key, e);
              return new KeyValue<String, DbRawData>(null, null);
            }
          })
          .filter((key, value) -> key != null && value != null);

      // Send to database output topic
      processedStream.to(DB_OUTPUT_TOPIC, Produced.with(Serdes.String(), dbValueSerde));

      KafkaStreams streams = new KafkaStreams(builder.build(), props);

      // Add exception handler
      streams.setUncaughtExceptionHandler(exception -> {
        logger.error("Uncaught exception in streams application", exception);
        return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
      });

      logger.info("Starting MQTT to Database streams...");
      streams.start();

      logger.info("MQTT to Database streams started successfully. Application is running...");

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
}