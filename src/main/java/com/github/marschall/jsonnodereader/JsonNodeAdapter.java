package com.github.marschall.jsonnodereader;

import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

final class JsonNodeAdapter {

  private JsonNodeAdapter() {
    throw new AssertionError("not instantiable");
  }

  static JsonValue adapt(JsonNode jsonNode) {
    return switch (jsonNode.getNodeType()) {
      case ARRAY -> new JsonNodeJsonArray(jsonNode);
      case BOOLEAN -> jsonNode.booleanValue() ? JsonValue.TRUE : JsonValue.FALSE;
      case NULL -> JsonValue.NULL;
      case NUMBER -> new JsonNodeJsonNumber(jsonNode);
      case OBJECT -> new JsonNodeJsonObject(jsonNode);
      case STRING -> new JsonNodeJsonString(jsonNode);
      default -> throw new IllegalArgumentException("Unexpected node type: " + jsonNode.getNodeType());
    };
  }
  
  static boolean valueEquals(JsonNode jsonNode, JsonValue jsonValue) {
    return switch (jsonValue.getValueType()) {
      case ARRAY -> jsonNode.isArray() && arrayEquals(jsonNode, (JsonArray) jsonValue);
      case FALSE -> jsonNode.isBoolean() && !jsonNode.booleanValue();
      case TRUE-> jsonNode.isBoolean() && jsonNode.booleanValue();
      case NULL -> jsonNode.isNull();
      case NUMBER -> jsonNode.isNumber() && numberEquals(jsonNode, (JsonNumber) jsonValue);
      case OBJECT -> jsonNode.isObject() && objectEquals(jsonNode, (JsonObject) jsonValue);
      case STRING -> jsonNode.isTextual() && textEquals(jsonNode, (JsonString) jsonValue);
      default -> throw new IllegalArgumentException("Unexpected node type: " + jsonNode.getNodeType());
    };
  }

  static boolean textEquals(JsonNode jsonNode, JsonString jsonString) {
    return jsonNode.textValue().equals(jsonString.getString());
  }

  static boolean objectEquals(JsonNode jsonNode, JsonObject jsonObject) {
    if (jsonNode.size() != jsonObject.size()) {
      return false;
    }
    for (Entry<String, JsonNode> property : jsonNode.properties()) {
      JsonValue jsonValue = jsonObject.get(property.getKey());
      if (jsonValue == null || !valueEquals(property.getValue(), jsonValue)) {
        return false;
      }
    }
    return true;
  }

  static boolean numberEquals(JsonNode jsonNode, JsonNumber jsonNumber) {
    return jsonNode.decimalValue().equals(jsonNumber.bigDecimalValue());
  }

  static boolean arrayEquals(JsonNode jsonNode, JsonArray jsonArray) {
    if (jsonNode.size() != jsonArray.size()) {
      return false;
    }
    for (int i = 0; i < jsonNode.size(); i++) {
      JsonNode element = jsonNode.get(i);
      JsonValue value = jsonArray.get(i);
      if (valueEquals(element, value)) {
        return false;
      }
    }
    return true;
  }
  
  // TODO toString support
  // TODO hashCode support

}
