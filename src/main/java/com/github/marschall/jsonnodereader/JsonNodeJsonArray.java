package com.github.marschall.jsonnodereader;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

final class JsonNodeJsonArray implements JsonArray {
  
  private final JsonNode jsonNode;

  JsonNodeJsonArray(JsonNode jsonNode) {
    Objects.requireNonNull(jsonNode, "jsonNode");
    this.jsonNode = jsonNode;
  }

  @Override
  public ValueType getValueType() {
    return ValueType.ARRAY;
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
  public boolean contains(Object o) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Iterator<JsonValue> iterator() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object[] toArray() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> T[] toArray(T[] a) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean add(JsonValue e) {
    throw new UnsupportedOperationException("add");
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException("remove");
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean addAll(Collection<? extends JsonValue> c) {
    throw new UnsupportedOperationException("addAll");
  }

  @Override
  public boolean addAll(int index, Collection<? extends JsonValue> c) {
    throw new UnsupportedOperationException("addAll");
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException("removeAll");
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException("retainAll");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("clear");
  }

  @Override
  public JsonValue get(int index) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JsonValue set(int index, JsonValue element) {
    throw new UnsupportedOperationException("set");
  }

  @Override
  public void add(int index, JsonValue element) {
    throw new UnsupportedOperationException("add");
  }

  @Override
  public JsonValue remove(int index) {
    throw new UnsupportedOperationException("remove");
  }

  @Override
  public int indexOf(Object o) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int lastIndexOf(Object o) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public ListIterator<JsonValue> listIterator() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ListIterator<JsonValue> listIterator(int index) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<JsonValue> subList(int fromIndex, int toIndex) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JsonObject getJsonObject(int index) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JsonArray getJsonArray(int index) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JsonNumber getJsonNumber(int index) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JsonString getJsonString(int index) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends JsonValue> List<T> getValuesAs(Class<T> clazz) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getString(int index) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getString(int index, String defaultValue) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getInt(int index) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getInt(int index, int defaultValue) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean getBoolean(int index) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean getBoolean(int index, boolean defaultValue) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isNull(int index) {
    // TODO Auto-generated method stub
    return false;
  }

}
