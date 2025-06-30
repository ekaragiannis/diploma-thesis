package com.example.kstreams;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MapJsonDeserializer implements Deserializer<Map<String, Double>> {
  private final ObjectMapper mapper = new ObjectMapper();

  public Map<String, Double> deserialize(String topic, byte[] data) {
    try {
      return mapper.readValue(data, new TypeReference<Map<String, Double>>() {
      });
    } catch (Exception e) {
      return null;
    }
  }
}
