package com.github.marschall.jsonnodereader;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

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
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean containsValue(Object value) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public JsonValue get(Object key) {
    // TODO Auto-generated method stub
    return null;
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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JsonObject getJsonObject(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JsonNumber getJsonNumber(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JsonString getJsonString(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getString(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getString(String name, String defaultValue) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getInt(String name) {
    // TODO Auto-generated method stub
    return 0;
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

}