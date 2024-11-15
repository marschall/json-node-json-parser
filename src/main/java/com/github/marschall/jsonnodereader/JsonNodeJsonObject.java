package com.github.marschall.jsonnodereader;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

final class JsonNodeJsonObject implements JsonObject {
  
  private final JsonNode jsonNode;

  JsonNodeJsonObject(JsonNode jsonNode) {
    Objects.requireNonNull(jsonNode, "jsonNode");
    this.jsonNode = jsonNode;
  }

  @Override
  public ValueType getValueType() {
    return ValueType.OBJECT;
  }

  @Override
  public int size() {
    return this.jsonNode.size();
  }

  @Override
  public boolean isEmpty() {
    return this.jsonNode.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    if (!(key instanceof String s)) {
      return false;
    }
    return this.jsonNode.get(s) != null;
  }

  @Override
  public boolean containsValue(Object value) {
    if (!(value instanceof JsonValue jsonValue)) {
      return false;
    }

    Iterator<JsonNode> elements = this.jsonNode.elements();
    while (elements.hasNext()) {
      JsonNode child = (JsonNode) elements.next();
      if (JsonNodeAdapter.valueEquals(child, jsonValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public JsonValue get(Object key) {
    if (!(key instanceof String s)) {
      return null;
    }
    JsonNode child = this.jsonNode.get(s);
    if (child != null) {
      return JsonNodeAdapter.adapt(child);
    } else {
      return null;
    }
  }

  @Override
  public JsonValue put(String key, JsonValue value) {
    throw new UnsupportedOperationException("put");
  }

  @Override
  public JsonValue remove(Object key) {
    throw new UnsupportedOperationException("remove");
  }

  @Override
  public void putAll(Map<? extends String, ? extends JsonValue> m) {
    throw new UnsupportedOperationException("putAll");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("putAll");
  }

  @Override
  public Set<String> keySet() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection<JsonValue> values() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Entry<String, JsonValue>> entrySet() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JsonArray getJsonArray(String name) {
    JsonNode value = this.jsonNode.get(name);
    if (value == null) {
      return null;
    }
    JsonNodeType nodeType = value.getNodeType();
    if (nodeType == JsonNodeType.ARRAY) {
      return new JsonNodeJsonArray(value);
    }
    throw new ClassCastException(JsonNodeType.ARRAY + " expected but got: " + nodeType);
  }

  @Override
  public JsonObject getJsonObject(String name) {
    JsonNode value = this.jsonNode.get(name);
    if (value == null) {
      return null;
    }
    JsonNodeType nodeType = value.getNodeType();
    if (nodeType == JsonNodeType.OBJECT) {
      return new JsonNodeJsonObject(value);
    }
    throw new ClassCastException(JsonNodeType.OBJECT + " expected but got: " + nodeType);
  }

  @Override
  public JsonNumber getJsonNumber(String name) {
    JsonNode value = this.jsonNode.get(name);
    if (value == null) {
      return null;
    }
    JsonNodeType nodeType = value.getNodeType();
    if (nodeType == JsonNodeType.NUMBER) {
      return new JsonNodeJsonNumber(value);
    }
    throw new ClassCastException(JsonNodeType.NUMBER + " expected but got: " + nodeType);
  }

  @Override
  public JsonString getJsonString(String name) {
    JsonNode value = this.jsonNode.get(name);
    if (value == null) {
      return null;
    }
    JsonNodeType nodeType = value.getNodeType();
    if (nodeType == JsonNodeType.STRING) {
      return new JsonNodeJsonString(value);
    }
    throw new ClassCastException(JsonNodeType.STRING + " expected but got: " + nodeType);
  }

  @Override
  public String getString(String name) {
    JsonNode value = this.jsonNode.get(name);
    if (value == null) {
      return null;
    }
    JsonNodeType nodeType = value.getNodeType();
    if (nodeType == JsonNodeType.STRING) {
      return value.textValue();
    }
    throw new ClassCastException(JsonNodeType.STRING + " expected but got: " + nodeType);
  }

  @Override
  public String getString(String name, String defaultValue) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getInt(String name) {
    JsonNode value = this.jsonNode.get(name);
    if (value == null) {
      throw new NullPointerException();
    }
    JsonNodeType nodeType = value.getNodeType();
    if (nodeType == JsonNodeType.NUMBER) {
      return value.intValue();
    }
    throw new ClassCastException(JsonNodeType.NUMBER + " expected but got: " + nodeType);
  }

  @Override
  public int getInt(String name, int defaultValue) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean getBoolean(String name) {
    JsonNode child = this.jsonNode.get(name);
    if (child.isBoolean()) {
      return child.booleanValue();
    } else {
      throw new ClassCastException();
    }
  }

  @Override
  public boolean getBoolean(String name, boolean defaultValue) {
    JsonNode child = this.jsonNode.get(name);
    if (child != null && child.isBoolean()) {
      return child.booleanValue();
    } else {
      return defaultValue;
    }
  }

  @Override
  public boolean isNull(String name) {
    return this.jsonNode.get(name).isNull();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof JsonObject other)) {
      return false;
    }
    return JsonNodeAdapter.objectEquals(this.jsonNode, other);
  }

  @Override
  public int hashCode() {
    return JsonNodeAdapter.objectHashCode(this.jsonNode);
  }

  @Override
  public String toString() {
    try {
      return JsonNodeAdapter.OBJECT_MAPPER.writeValueAsString(this.jsonNode);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("could not serialize JsonNode", e);
    }
  }

}
