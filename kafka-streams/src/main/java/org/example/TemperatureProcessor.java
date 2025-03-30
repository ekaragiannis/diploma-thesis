package org.example;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class TemperatureProcessor {
    private static final String SCHEMA_REGISTRY_URL = "http://schema-registry:8081";
    private static final String INPUT_TOPIC = "raw-data";
    private static final String OUTPUT_TOPIC = "streams-processed-data";

    public static void process() throws InterruptedException {
        // Kafka Streams configuration
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class.getName());
        props.put("schema.registry.url", SCHEMA_REGISTRY_URL);

        // Create a StreamsBuilder
        StreamsBuilder builder = new StreamsBuilder();

        // Define the stream processing topology
        builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), new GenericAvroSerde()))
                .mapValues(value -> convertTemperature((GenericRecord) value)) // Convert F to C
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String())); // Produce to output

        Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, props);

        // Add a shutdown hook to gracefully close the Kafka Streams application
        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Kafka Streams application...");
            streams.close();
            latch.countDown();
        }));

        System.out.println("Starting Kafka Streams application...");
        streams.start();

        latch.await();
    }

    private static String convertTemperature(GenericRecord record) {
        try {
            // Extract fields from the Avro record
            String id = record.get("id") != null ? record.get("id").toString() : "unknown";
            int tempF = record.get("temperature") != null ? (Integer) record.get("temperature") : 0;

            // Convert Fahrenheit to Celsius
            int tempC = (tempF - 32) * 5 / 9;

            // Log the results
            System.out.println("ID: " + id);
            System.out.println("Temp F: " + tempF);
            System.out.println("Temp C: " + tempC);

            // Return the result as a JSON string
            return "{ \"id\": \"" + id + "\", \"temperature\": " + tempC + " }";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}