package com.github.marschall.jsonnodereader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.TestAbortedException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;
import jakarta.json.stream.JsonParserFactory;

class JsonNodeJsonParserTests {

  private static final JsonParserFactory PARSER_FACTORY = Json.createParserFactory(null);

  private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
      .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
      .build();

  private static final String SAMPLE_JSON = "{\"key1\":[1,1234567890,1.1,true,false,null],\"key2\":[\"string\",-2]}";

  private static final String NESTED_JSON_INPUT = "[{\"key1\":[1]},2,[{\"key2\":3}],4]";
  private static final String NESTED_JSON_OUTPUT = "[2,4]";

  private static final String NUMBERS = "[1, 1.0, 2147483647, -2147483648, 2147483648, -2147483649, 9223372036854775807, -9223372036854775808, 9223372036854775808, -9223372036854775809, 1.1]";

  private static final String LITERALS = "[null, true, false, \"hello\\\"world\", 1]";
  
  private static final String EMPTY_STRUCTURES = "[[], {}]";
  
  private static final String STRUCTURES = "[[null, true, false, 1, \"one\", {\"key\": \"value\"}], "
      + "{\"key1\": true, \"key2\": false, \"key3\": 1, \"key4\": [\"value4\"], \"key5\": \"value5\", \"key6\": null}]";

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

  @ParameterizedTest
  @MethodSource("parsers")
  void numbers(StringParserFactory stringParserFactory) throws IOException {
    try (JsonParser jsonParser = stringParserFactory.parse(NUMBERS)) {
      assertNumbers(jsonParser);
    }
  }

  @ParameterizedTest
  @MethodSource("parsers")
  void literals(StringParserFactory stringParserFactory) throws IOException {
    try (JsonParser jsonParser = stringParserFactory.parse(LITERALS)) {
      assertLiterals(jsonParser);
    }
  }

  @ParameterizedTest
  @MethodSource("parsers")
  void emptyStructure(StringParserFactory stringParserFactory) throws IOException {
    try (JsonParser jsonParser = stringParserFactory.parse(EMPTY_STRUCTURES)) {
      assertEmptyStructures(jsonParser);
    }
  }

  @ParameterizedTest
  @MethodSource("parsers")
  void unsupportedListOperations(StringParserFactory stringParserFactory) throws IOException {
    try (JsonParser jsonParser = stringParserFactory.parse(EMPTY_STRUCTURES)) {
      assertSame(Event.START_ARRAY, jsonParser.next());
      assertSame(Event.START_ARRAY, jsonParser.next());
      JsonArray array = assumeDoesNotThrow(UnsupportedOperationException.class, jsonParser::getArray);
      assertThrows(UnsupportedOperationException.class, () -> array.add(Json.createValue(42)));
    }
  }
  
  @ParameterizedTest
  @MethodSource("parsers")
  void getObjectRoot(StringParserFactory stringParserFactory) throws IOException {
    try (JsonParser jsonParser = stringParserFactory.parse(EMPTY_STRUCTURES)) {
      assertSame(Event.START_ARRAY, jsonParser.next());
      JsonArray array = assumeDoesNotThrow(UnsupportedOperationException.class, jsonParser::getArray);
      assertThrows(UnsupportedOperationException.class, () -> array.add(Json.createValue(42)));
    }
  }

  @ParameterizedTest
  @MethodSource("parsers")
  void unsupportedMapOperations(StringParserFactory stringParserFactory) throws IOException {
    try (JsonParser jsonParser = stringParserFactory.parse(EMPTY_STRUCTURES)) {
      assertSame(Event.START_ARRAY, jsonParser.next());
      assertSame(Event.START_ARRAY, jsonParser.next());
      assertSame(Event.END_ARRAY, jsonParser.next());
      assertSame(Event.START_OBJECT, jsonParser.next());
      JsonObject object = assumeDoesNotThrow(UnsupportedOperationException.class, jsonParser::getObject);
      assertThrows(UnsupportedOperationException.class, () -> object.put("new", Json.createValue(42)));
    }
  }

  @ParameterizedTest
  @MethodSource("parsers")
  void structure(StringParserFactory stringParserFactory) throws IOException {
    try (JsonParser jsonParser = stringParserFactory.parse(STRUCTURES)) {
      assertStructures(jsonParser);
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
    JsonNode jacksonNode = OBJECT_MAPPER.readTree(json);
    return new JsonNodeJsonParser(jacksonNode);
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

  private static void assertNumbers(JsonParser jsonParser) {
    assertSame(Event.START_ARRAY, jsonParser.next());

    // 1
    assertSame(Event.VALUE_NUMBER, jsonParser.next());
    assertEquals(1, jsonParser.getInt());
    assertEquals(1, jsonParser.getLong());
    assertEquals(0, jsonParser.getBigDecimal().compareTo(BigDecimal.ONE));
    assertTrue(jsonParser.isIntegralNumber());
    try {
      JsonNumber number = (JsonNumber) jsonParser.getValue();
      assertSame(ValueType.NUMBER, number.getValueType());
      assertTrue(number.isIntegral());
      Number numberValue = number.numberValue();
      assertTrue(numberValue instanceof Integer);
      assertEquals(Integer.valueOf(1), numberValue);
      assertEquals("1", numberValue.toString());
    } catch (UnsupportedOperationException e) {
      // is optional
    }

    assertSame(Event.VALUE_NUMBER, jsonParser.next());
    assertEquals(1, jsonParser.getInt());
    assertEquals(1, jsonParser.getLong());
    assertEquals(0, jsonParser.getBigDecimal().compareTo(BigDecimal.ONE));
    assertFalse(jsonParser.isIntegralNumber());
    try {
      JsonNumber number = (JsonNumber) jsonParser.getValue();
      assertSame(ValueType.NUMBER, number.getValueType());
      assertFalse(number.isIntegral());
      Number numberValue = number.numberValue();
      assertTrue(numberValue instanceof BigDecimal);
      assertEquals(0, ((BigDecimal) numberValue).compareTo(BigDecimal.ONE));
      assertEquals(1, number.intValue());
      assertEquals(1, number.intValueExact());
      assertEquals(1L, number.longValue());
      assertEquals(1L, number.longValueExact());
      assertEquals(BigInteger.ONE, number.bigIntegerValue());
      assertEquals(BigInteger.ONE, number.bigIntegerValueExact());
      assertEquals(1.0d, number.doubleValue(), 0.0001d);
    } catch (UnsupportedOperationException e) {
      // is optional
    }

    assertSame(Event.VALUE_NUMBER, jsonParser.next());
    assertEquals(Integer.MAX_VALUE, jsonParser.getInt());
    assertEquals(Integer.MAX_VALUE, jsonParser.getLong());

    assertSame(Event.VALUE_NUMBER, jsonParser.next());
    assertEquals(Integer.MIN_VALUE, jsonParser.getInt());
    assertEquals(Integer.MIN_VALUE, jsonParser.getLong());

    assertSame(Event.VALUE_NUMBER, jsonParser.next());
    assertEquals(Integer.MIN_VALUE, jsonParser.getInt());
    assertEquals(Integer.MAX_VALUE + 1L, jsonParser.getLong());
    try {
      JsonNumber number = (JsonNumber) jsonParser.getValue();
      assertSame(ValueType.NUMBER, number.getValueType());
      assertTrue(number.isIntegral());
      Number numberValue = number.numberValue();
      assertTrue(numberValue instanceof Long);
      assertEquals(Long.valueOf(Integer.MAX_VALUE + 1L), numberValue);
      assertThrows(ArithmeticException.class, number::intValueExact);
      assertEquals(Integer.MAX_VALUE + 1L, number.longValueExact());
      assertEquals(BigDecimal.valueOf(Integer.MAX_VALUE + 1L), number.bigDecimalValue());
    } catch (UnsupportedOperationException e) {
      // is optional
    }

    assertSame(Event.VALUE_NUMBER, jsonParser.next());
    assertEquals(Integer.MAX_VALUE, jsonParser.getInt());
    assertEquals(Integer.MIN_VALUE - 1L, jsonParser.getLong());

    assertSame(Event.VALUE_NUMBER, jsonParser.next());
    assertEquals(-1, jsonParser.getInt());
    assertEquals(Long.MAX_VALUE, jsonParser.getLong());

    assertSame(Event.VALUE_NUMBER, jsonParser.next());
    assertEquals(0, jsonParser.getInt());
    assertEquals(Long.MIN_VALUE, jsonParser.getLong());

    assertSame(Event.VALUE_NUMBER, jsonParser.next());
    assertEquals(0, jsonParser.getInt());
    assertEquals(Long.MIN_VALUE, jsonParser.getLong());
    assertEquals(new BigDecimal("9223372036854775808"), jsonParser.getBigDecimal());

    assertSame(Event.VALUE_NUMBER, jsonParser.next());
    assertEquals(-1, jsonParser.getInt());
    assertEquals(Long.MAX_VALUE, jsonParser.getLong());
    assertEquals(new BigDecimal("-9223372036854775809"), jsonParser.getBigDecimal());

    assertSame(Event.VALUE_NUMBER, jsonParser.next());
    try {
      JsonNumber number = (JsonNumber) jsonParser.getValue();
      assertSame(ValueType.NUMBER, number.getValueType());
      assertFalse(number.isIntegral());
      assertThrows(ArithmeticException.class, number::intValueExact);
      assertThrows(ArithmeticException.class, number::longValueExact);
      assertThrows(ArithmeticException.class, number::bigIntegerValueExact);
      assertEquals("1.1", number.toString());
      assertEquals(new BigDecimal("1.1").hashCode(), number.hashCode());
      assertEquals(Json.createValue(new BigDecimal("1.1")), number);
      assertEquals(number, Json.createValue(new BigDecimal("1.1")));
    } catch (UnsupportedOperationException e) {
      // is optional
    }

    assertSame(Event.END_ARRAY, jsonParser.next());
    assertFalse(jsonParser.hasNext());
  }


  private static void assertLiterals(JsonParser jsonParser) {
    assertSame(Event.START_ARRAY, jsonParser.next());

    assertSame(Event.VALUE_NULL, jsonParser.next());
    JsonValue value = assumeDoesNotThrow(UnsupportedOperationException.class, jsonParser::getValue);
    assertSame(ValueType.NULL, value.getValueType());

    assertSame(Event.VALUE_TRUE, jsonParser.next());
    value = jsonParser.getValue();
    assertSame(ValueType.TRUE, value.getValueType());

    assertSame(Event.VALUE_FALSE, jsonParser.next());
    value = jsonParser.getValue();
    assertSame(ValueType.FALSE, value.getValueType());

    assertSame(Event.VALUE_STRING, jsonParser.next());
    value = jsonParser.getValue();
    assertSame(ValueType.STRING, value.getValueType());
    JsonString jsonString = (JsonString) value;
    assertEquals("hello\"world", jsonString.getString());
    assertEquals("hello\"world", jsonString.getChars().toString());
    assertEquals("hello\"world".hashCode(), jsonString.hashCode());
    assertNotEquals(null, jsonString);
    assertEquals(jsonString, jsonString);
    assertNotEquals(jsonString, "hello\"world");
    assertEquals(Json.createValue("hello\"world"), jsonString);
    assertEquals(jsonString, Json.createValue("hello\"world"));
    assertEquals("\"hello\\\"world\"", jsonString.toString());

    assertSame(Event.VALUE_NUMBER, jsonParser.next());
    value = jsonParser.getValue();
    assertSame(ValueType.NUMBER, value.getValueType());
    JsonNumber jsonNumber = (JsonNumber) value;
    assertEquals(jsonNumber, jsonNumber);
    assertNotEquals(jsonNumber, 1);
    assertNotEquals(jsonNumber, 1L);
    assertNotEquals(jsonNumber, BigDecimal.ONE);
    assertEquals(jsonNumber, Json.createValue(1));

    assertSame(Event.END_ARRAY, jsonParser.next());
    assertFalse(jsonParser.hasNext());
  }

  private static void assertEmptyStructures(JsonParser jsonParser) {
    assertSame(Event.START_ARRAY, jsonParser.next());

    assertSame(Event.START_ARRAY, jsonParser.next());
    JsonArray jsonArray = assumeDoesNotThrow(UnsupportedOperationException.class, jsonParser::getArray);
    assertEquals(JsonValue.EMPTY_JSON_ARRAY, jsonArray);
    assertEquals(JsonValue.EMPTY_JSON_ARRAY.hashCode(), jsonArray.hashCode());
    assertEquals(List.of().hashCode(), jsonArray.hashCode());
    assertEquals("[]", jsonArray.toString());
    assertSame(Event.END_ARRAY, jsonParser.currentEvent());

    JsonLocation location = jsonParser.getLocation();
    assertNotNull(location);
    long streamOffset = location.getStreamOffset();
    assertTrue(streamOffset == -1L || streamOffset > 0L);
    long lineNumber = location.getLineNumber();
    assertTrue(lineNumber == -1L || lineNumber > 0L);
    long columnNumber = location.getColumnNumber();
    assertTrue(columnNumber == -1L || columnNumber > 0L);

    assertSame(Event.START_OBJECT, jsonParser.next());
    JsonObject jsonObject = jsonParser.getObject();
    assertEquals(JsonValue.EMPTY_JSON_OBJECT, jsonObject);
    assertEquals(JsonValue.EMPTY_JSON_OBJECT.hashCode(), jsonObject.hashCode());
    assertEquals(Map.of().hashCode(), jsonObject.hashCode());
    assertEquals("{}", jsonObject.toString());
    assertSame(Event.END_OBJECT, jsonParser.currentEvent());

    assertSame(Event.END_ARRAY, jsonParser.next());
    assertFalse(jsonParser.hasNext());
  }

  private static void assertStructures(JsonParser jsonParser) {
    assertSame(Event.START_ARRAY, jsonParser.next());

    assertSame(Event.START_ARRAY, jsonParser.next());
    JsonArray jsonArray = assumeDoesNotThrow(UnsupportedOperationException.class, jsonParser::getArray);
    assertSame(ValueType.ARRAY, jsonArray.getValueType());
    assertEquals(6, jsonArray.size());
    assertFalse(jsonArray.isEmpty());
    assertTrue(jsonArray.contains(Json.createValue(1)));
    assertFalse(jsonArray.contains(Json.createValue(2)));

    assertTrue(jsonArray.isNull(0));
    assertFalse(jsonArray.isNull(1));
    assertEquals(3, jsonArray.indexOf(Json.createValue(1)));
    assertEquals(3, jsonArray.lastIndexOf(Json.createValue(1)));
    assertEquals(-1, jsonArray.indexOf(Json.createValue(2)));
    assertEquals(-1, jsonArray.lastIndexOf(Json.createValue(2)));
    assertEquals(-1, jsonArray.indexOf("2"));
    assertEquals(-1, jsonArray.lastIndexOf("2"));

    assertTrue(jsonArray.isNull(0));
    assertFalse(jsonArray.isNull(1));
    assertTrue(jsonArray.getBoolean(1));
    assertTrue(jsonArray.getBoolean(1, false));
    assertFalse(jsonArray.getBoolean(2));
    assertFalse(jsonArray.getBoolean(2, true));

    assertFalse(jsonArray.getBoolean(-1, false));
    assertTrue(jsonArray.getBoolean(-1, true));
    assertFalse(jsonArray.getBoolean(3, false));
    assertTrue(jsonArray.getBoolean(3, true));
    assertFalse(jsonArray.getBoolean(6, false));
    assertTrue(jsonArray.getBoolean(6, true));

    assertEquals(22, jsonArray.getInt(-1, 22));
    assertEquals(22, jsonArray.getInt(0, 22));
    assertEquals(22, jsonArray.getInt(1, 22));
    assertEquals(1, jsonArray.getInt(3, 22));
    assertEquals(22, jsonArray.getInt(6, 22));

    assertEquals("two", jsonArray.getString(-1, "two"));
    assertEquals("two", jsonArray.getString(0, "two"));
    assertEquals("two", jsonArray.getString(1, "two"));
    assertEquals("one", jsonArray.getString(4, "two"));
    assertEquals("two", jsonArray.getString(6, "two"));

    assertEquals(1, jsonArray.getInt(3));
    JsonNumber jsonNumber = jsonArray.getJsonNumber(3);
    assertEquals(Json.createValue(1), jsonNumber);

    assertEquals("one", jsonArray.getString(4));
    JsonString jsonString = jsonArray.getJsonString(4);
    assertEquals(Json.createValue("one"), jsonString);
    
    JsonObject jsonObjectInArray = jsonArray.getJsonObject(5);
    JsonObject expectedObject = Json.createObjectBuilder()
            .add("key", "value")
            .build();
    assertEquals(expectedObject, jsonObjectInArray);
    assertEquals(expectedObject.toString(), jsonObjectInArray.toString());
    assertEquals(expectedObject.hashCode(), jsonObjectInArray.hashCode());
    assertEquals(jsonObjectInArray, expectedObject);
    assertEquals(jsonObjectInArray, jsonObjectInArray);
    assertNotEquals(jsonObjectInArray, Map.of("key", "value"));

    JsonArray expectedArray = Json.createArrayBuilder()
        .addNull()
        .add(true)
        .add(false)
        .add(1)
        .add("one")
        .add(Json.createObjectBuilder().add("key", "value").build())
        .build();
    assertEquals(expectedArray, jsonArray);
    assertEquals(jsonArray, jsonArray);
    assertEquals(jsonArray, expectedArray);
    assertEquals(expectedArray.hashCode(), jsonArray.hashCode());
    assertSame(Event.END_ARRAY, jsonParser.currentEvent());

    assertSame(Event.START_OBJECT, jsonParser.next());
    JsonObject jsonObject = jsonParser.getObject();
    assertSame(ValueType.OBJECT, jsonObject.getValueType());
    assertFalse(jsonObject.isEmpty());
    assertEquals(6, jsonObject.size());
    
    assertTrue(jsonObject.containsKey("key1"));
    assertFalse(jsonObject.containsKey("key0"));
    assertFalse(jsonObject.containsKey(new Object()));
    
    assertTrue(jsonObject.containsValue(Json.createValue("value5")));
    assertFalse(jsonObject.containsValue("value5"));
    assertFalse(jsonObject.containsValue(Json.createValue("value6")));
    
    assertTrue(jsonObject.getBoolean("key1"));
    assertFalse(jsonObject.getBoolean("key2"));
    assertEquals(1, jsonObject.getInt("key3"));
    assertEquals(Json.createValue(1), jsonObject.getJsonNumber("key3"));
    assertEquals(Json.createArrayBuilder().add("value4").build(), jsonObject.getJsonArray("key4"));
    assertEquals(Json.createValue("value5"), jsonObject.getJsonString("key5"));
    assertEquals("value5", jsonObject.getString("key5"));

    assertFalse(jsonObject.isNull("key1"));
    assertTrue(jsonObject.isNull("key6"));
    assertThrows(NullPointerException.class, () -> jsonObject.isNull("key7"));

    assertTrue(jsonObject.getBoolean("key1", false));
    assertTrue(jsonObject.getBoolean("key1", true));
    assertFalse(jsonObject.getBoolean("key2", false));
    assertFalse(jsonObject.getBoolean("key2", true));

    assertFalse(jsonObject.getBoolean("n/a", false));
    assertTrue(jsonObject.getBoolean("n/a", true));
    assertFalse(jsonObject.getBoolean("key3", false));
    assertTrue(jsonObject.getBoolean("key3", true));

    assertEquals(22, jsonObject.getInt("n/a", 22));
    assertEquals(22, jsonObject.getInt("key1", 22));
    assertEquals(22, jsonObject.getInt("key2", 22));
    assertEquals(1, jsonObject.getInt("key3", 22));

    assertEquals("two", jsonObject.getString("n/a", "two"));
    assertEquals("two", jsonObject.getString("key1", "two"));
    assertEquals("two", jsonObject.getString("key2", "two"));
    assertEquals("value5", jsonObject.getString("key5", "value4"));
    JsonArray jsonArrayInObject = jsonObject.getJsonArray("key4");
    expectedArray = Json.createArrayBuilder().add("value4").build();
    assertEquals(expectedArray, jsonArrayInObject);
    assertEquals(expectedArray.toString(), jsonArrayInObject.toString());
    assertEquals(jsonArrayInObject, expectedArray);
    assertNull(jsonObject.get("key7"));
    assertNull(jsonObject.getJsonNumber("key7"));
    assertNull(jsonObject.getJsonString("key7"));
    assertThrows(NullPointerException.class, () -> jsonObject.getString("key7"));
    assertThrows(NullPointerException.class, () -> jsonObject.getInt("key7"));
    assertThrows(NullPointerException.class, () -> jsonObject.getBoolean("key7"));
    assertNull(jsonObject.getJsonArray("key7"));
    assertNull(jsonObject.getJsonObject("key7"));
    assertNull(jsonObject.get(Json.createValue("key5")));

    assertSame(Event.END_OBJECT, jsonParser.currentEvent());

    assertSame(Event.END_ARRAY, jsonParser.next());
    assertFalse(jsonParser.hasNext());
  }

  private static <T> T assumeDoesNotThrow(Class<? extends RuntimeException> exceptionClass, Supplier<T> supplier) {
    try {
      return supplier.get();
    } catch (RuntimeException e) {
      if (exceptionClass.isInstance(e)) {
        throw new TestAbortedException();
      } else {
        throw e;
      }
    }
  }

}
