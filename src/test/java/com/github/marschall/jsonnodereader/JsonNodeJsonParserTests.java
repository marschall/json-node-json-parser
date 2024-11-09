package com.github.marschall.jsonnodereader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;
import jakarta.json.stream.JsonParserFactory;

class JsonNodeJsonParserTests {

  private static final JsonParserFactory PARSER_FACTORY = Json.createParserFactory(null);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String SAMPLE_JSON = "{\"key1\":[1,1234567890,1.1,true,false,null],\"key2\":[\"string\",-2]}";

  private static final String NESTED_JSON_INPUT = "[{\"key1\":[1]},2,[{\"key2\":3}],4]";
  private static final String NESTED_JSON_OUTPUT = "[2,4]";
  
  static List<Arguments> parsers() {
    return List.of(
        Arguments.of((StringParserFactory) JsonNodeJsonParserTests::defaultParser),
        Arguments.of((StringParserFactory) JsonNodeJsonParserTests::jsonStructureParser),
        Arguments.of((StringParserFactory) JsonNodeJsonParserTests::jsonNodeJsonParser)
        );
  }

  @ParameterizedTest
  @MethodSource("parsers")
  void roundTrip(StringParserFactory stringParserFactory) throws IOException {
    try (JsonParser jsonParser = stringParserFactory.parse(SAMPLE_JSON)) {
      assertRoundTrip(jsonParser);
    }
  }

  @ParameterizedTest
  @MethodSource("parsers")
  void skip(StringParserFactory stringParserFactory) throws IOException {
    try (JsonParser jsonParser = stringParserFactory.parse(NESTED_JSON_INPUT)) {
      assertRoundTripSkip(jsonParser);
    }
  }

  @FunctionalInterface
  interface StringParserFactory {

    JsonParser parse(String json) throws IOException;

  }

  private static JsonParser defaultParser(String json) {
    return Json.createParser(new StringReader(json));
  }

  private static JsonParser jsonStructureParser(String json) {
    try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
      JsonStructure jsonStructor = jsonReader.read();
      return switch (jsonStructor) {
        case JsonArray jsonArray -> PARSER_FACTORY.createParser(jsonArray);
        case JsonObject jsonObject -> PARSER_FACTORY.createParser(jsonObject);
        default -> throw new IllegalStateException("Unexpected value");
      };
    }
  }

  private static JsonParser jsonNodeJsonParser(String json) throws JacksonException {
    JsonNode jsonNode = OBJECT_MAPPER.readTree(json);
    return new JsonNodeJsonParser(jsonNode);
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
    assertEquals(SAMPLE_JSON, output);
  }

  private static void assertRoundTripSkip(JsonParser jsonParser) throws IOException {
    String output;
    try (StringWriter stringWriter = new StringWriter();
        JsonGenerator jsonWriter = Json.createGenerator(stringWriter)) {
      boolean firstStartArray = true;
      while (jsonParser.hasNext()) {
        switch (jsonParser.next()) {
        case KEY_NAME -> jsonWriter.writeKey(jsonParser.getString());
        case START_ARRAY -> {
          if (firstStartArray) {
            jsonWriter.writeStartArray();
            firstStartArray = false;
          } else {
            jsonParser.skipArray();
            try {
              assertSame(Event.END_ARRAY, jsonParser.currentEvent());
            } catch (Exception e) {
              // optional method
            }
          }
        }
        case START_OBJECT -> {
          jsonParser.skipObject();
          try {
            assertSame(Event.END_OBJECT, jsonParser.currentEvent());
          } catch (UnsupportedOperationException e) {
            // optional method
          }
        }
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
    assertEquals(NESTED_JSON_OUTPUT, output);
  }

  private static void assertRoundTrip2(JsonParser jsonParser) throws IOException {
    System.out.println("====");
    while (jsonParser.hasNext()) {
      System.out.println(jsonParser.next().name());
    }
  }

}