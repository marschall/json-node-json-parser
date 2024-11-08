package com.github.marschall.jsonnodereader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;

class JsonNodeJsonParserTests {

  @Test
  void roundTrip() throws IOException {
    String input = "{\"key1\": [1, 1234567890, 1.1, true, false, null], \"key2\": [\"string\", -2]}";
    String output;
    JsonNode jsonNode = new ObjectMapper().readTree(input);
    try (JsonParser jsonParser = new JsonNodeJsonParser(jsonNode);
        StringWriter stringWriter = new StringWriter();
        JsonGenerator jsonWriter = Json.createGenerator(stringWriter)) {
      while (jsonParser.hasNext()) {
        switch (jsonParser.next()) {
          case KEY_NAME -> jsonWriter.writeKey(jsonParser.getString());
          case START_ARRAY -> jsonWriter.writeStartArray();
          case START_OBJECT -> jsonWriter.writeStartObject();
          case END_ARRAY, END_OBJECT -> jsonWriter.writeEnd();
          case VALUE_TRUE -> jsonWriter.write(true);
          case VALUE_FALSE -> jsonWriter.write(false);
          case VALUE_NULL -> jsonWriter.writeNull();
          case VALUE_NUMBER -> jsonWriter.write(jsonParser.getBigDecimal());
          case VALUE_STRING -> jsonWriter.write(jsonParser.getString());
          default -> {
            fail("unexpeted event");
            break;
          }
        }
      }
      output = stringWriter.toString();
    }
    assertEquals(input, output);
  }

}
