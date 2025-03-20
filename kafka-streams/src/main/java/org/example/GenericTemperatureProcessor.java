package org.example;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class GenericTemperatureProcessor {

    public static void main(String[] args) {
        // Define the Avro schema programmatically
        final Schema schema = SchemaBuilder
                .record("Temperature")
                .namespace("com.example.kafkastreams")
                .fields()
                .name("id").type().intType().noDefault()
                .name("temperature").type().floatType().noDefault()
                .endRecord();

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "temperature-processor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "http://localhost:9092"));
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        props.put("schema.registry.url", System.getenv().getOrDefault("SCHEMA_REGISTRY_URL", "http://localhost:8081"));

        final StreamsBuilder builder = new StreamsBuilder();

        // Configure Avro Serde for GenericRecord
        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url",
                props.getProperty("schema.registry.url"));
        final GenericAvroSerde avroSerde = new GenericAvroSerde();
        avroSerde.configure(serdeConfig, false);

        // Create a stream from the input topic
        builder.stream("raw-data", Consumed.with(Serdes.String(), avroSerde))
                .peek((key, value) -> {
                    System.out.println("Processing record with key: " + key);
                    System.out.println("ID: " + value.get("id") + ", Temperature: " + value.get("temperature"));
                })
                .mapValues(genericRecord -> {
                    // Create a new record with transformed values
                    GenericRecord newRecord = new GenericData.Record(schema);

                    // Copy the ID as is
                    newRecord.put("id", genericRecord.get("id"));

                    // Process the temperature (add 1.0)
                    float currentTemp = (Float) genericRecord.get("temperature");
                    newRecord.put("temperature", (currentTemp - 32) * 5 / 9);
                    System.out.println(newRecord);
                    return newRecord;
                })
                .to("streams-processed-data", Produced.with(Serdes.String(), avroSerde));

        final KafkaStreams streams = new KafkaStreams(builder.build(), props);
        final CountDownLatch latch = new CountDownLatch(1);

        // Attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-temperature-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (final Throwable e) {
            System.err.println("Error starting Kafka Streams: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}