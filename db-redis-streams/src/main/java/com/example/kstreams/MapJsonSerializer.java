package com.example.kstreams;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MapJsonSerializer implements Serializer<Map<String, Double>> {
  private final ObjectMapper mapper = new ObjectMapper();

  public byte[] serialize(String topic, Map<String, Double> data) {
    try {
      return mapper.writeValueAsBytes(data);
    } catch (Exception e) {
      return null;
    }
  }
}
