package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * A Kafka Streams application that processes temperature data from JSON to Avro
 * format,
 * converting Fahrenheit to Celsius in the process.
 */
public class GenericTemperatureProcessor {
    private static final String TOPIC_SOURCE = "raw-data";
    private static final String TOPIC_TARGET = "streams-processed-data";
    private static final String APP_ID = "temperature-processor";

    public static void main(String[] args) {
        final Schema schema = createTemperatureSchema();
        Properties props = configureProperties();
        System.out.println(props);

        final Map<String, String> serdeConfig = Collections.singletonMap(
                "schema.registry.url", props.getProperty("schema.registry.url"));

        final StreamsBuilder builder = new StreamsBuilder();
        final ObjectMapper objectMapper = new ObjectMapper();

        // Avro Serde Setup
        final GenericAvroSerde avroSerde = new GenericAvroSerde();
        avroSerde.configure(serdeConfig, false);

        // ✅ Consume JSON messages and convert them into Avro format
        builder.stream(TOPIC_SOURCE, Consumed.with(Serdes.String(), Serdes.String()))
                .map((key, value) -> fahrenheitToCelsius(value, schema, objectMapper))
                .to(TOPIC_TARGET, Produced.with(Serdes.String(), avroSerde));

        runStreamsApplication(builder, props);
    }

    private static Schema createTemperatureSchema() {
        return SchemaBuilder
                .record("Temperature")
                .namespace("com.example.kafkastreams")
                .fields()
                .name("id").type().intType().noDefault()
                .name("temperature").type().floatType().noDefault()
                .endRecord();
    }

    private static Properties configureProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_ID);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
                System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"));
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        props.put("schema.registry.url",
                System.getenv().getOrDefault("SCHEMA_REGISTRY_URL", "http://localhost:8081"));
        return props;
    }

    private static KeyValue<String, GenericRecord> fahrenheitToCelsius(
            String jsonString, Schema schema, ObjectMapper objectMapper) {
        System.out.println("Processing JSON: " + jsonString);
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);

            // Extract `id` as the Kafka key
            String key = jsonNode.get("id").asText();

            // Create a new Avro record
            GenericRecord newValue = new GenericData.Record(schema);
            newValue.put("id", jsonNode.get("id").asInt());

            // Convert Fahrenheit to Celsius
            double fahrenheit = jsonNode.get("temperature").asDouble();
            float celsius = (float) ((fahrenheit - 32) * 5 / 9);
            newValue.put("temperature", celsius);

            KeyValue<String, GenericRecord> pair = KeyValue.pair(key, newValue);
            System.out.println(pair);
            return pair;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON: " + jsonString, e);
        }
    }

    private static void runStreamsApplication(StreamsBuilder builder, Properties props) {
        final KafkaStreams streams = new KafkaStreams(builder.build(), props);
        final CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread("streams-temperature-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            System.out.println("Temperature processor started successfully");
            latch.await();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Application interrupted: " + e.getMessage());
        } catch (final Exception e) {
            System.err.println("Error running Kafka Streams application: " + e.getMessage());
            e.printStackTrace();
        } finally {
            streams.close();
            System.exit(0);
        }
    }
}