package com.github.marschall.jsonnodereader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;

class JsonNodeJsonParserTests {

  private static final String JSON = "{\"key1\":[1,1234567890,1.1,true,false,null],\"key2\":[\"string\",-2]}";

  @Test
  void roundTrip() throws IOException {
    try (JsonParser jsonParser = Json.createParser(new StringReader(JSON))) {
      assertRoundTrip(jsonParser);
    }
    try (JsonReader jsonReader = Json.createReader(new StringReader(JSON))) {
      JsonObject jsonObject = jsonReader.readObject();
      try (JsonParser jsonParser = Json.createParserFactory(null).createParser(jsonObject)) {
        assertRoundTrip(jsonParser);
      }
    }
    JsonNode jsonNode = new ObjectMapper().readTree(JSON);
    try (JsonParser jsonParser = new JsonNodeJsonParser(jsonNode)) {
      assertRoundTrip(jsonParser);
    }
  }
  
  private static void assertRoundTrip(JsonParser jsonParser) throws IOException {
    String output;
    try (StringWriter stringWriter = new StringWriter();
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
        };
    }
      jsonWriter.flush();
      output = stringWriter.toString();
    }
    assertEquals(JSON, output);
  }
    
  private static void assertRoundTrip2(JsonParser jsonParser) throws IOException {
    System.out.println("====");
      while (jsonParser.hasNext()) {
        System.out.println(jsonParser.next().name());
      }
  }

}
