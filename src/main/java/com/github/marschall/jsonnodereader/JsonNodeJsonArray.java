package com.github.marschall.jsonnodereader;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Consumer;

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
    Objects.requireNonNull(o);
    if (!(o instanceof JsonValue jsonValue)) {
      return false;
    }
    for (int i = 0; i < this.size(); i++) {
      if (JsonNodeAdapter.valueEquals(this.jsonNode.get(i), jsonValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Iterator<JsonValue> iterator() {
    return new JsonValueIterator();
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
    for (Object object : c) {
      if (!this.contains(object)) {
        return false;
      }
    }
    return true;
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
    JsonNode value = this.jsonNode.get(Objects.checkIndex(index, this.size()));
    return JsonNodeAdapter.adapt(value);
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
    if (!(o instanceof JsonValue jsonValue)) {
      return -1;
    }
    for (int i = 0; i < this.size(); i++) {
      if (JsonNodeAdapter.valueEquals(this.jsonNode.get(i), jsonValue)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public int lastIndexOf(Object o) {
    if (!(o instanceof JsonValue jsonValue)) {
      return -1;
    }
    for (int i = this.size() -1 ; i >= 0; i--) {
      if (JsonNodeAdapter.valueEquals(this.jsonNode.get(i), jsonValue)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public ListIterator<JsonValue> listIterator() {
    return new JsonValueIterator();
  }

  @Override
  public ListIterator<JsonValue> listIterator(int index) {
    return new JsonValueIterator(Objects.checkIndex(index, this.size()));
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
    JsonNode value = this.jsonNode.get(Objects.checkIndex(index, this.size()));
    return value.isNull();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof JsonArray other)) {
      return false;
    }
    return JsonNodeAdapter.arrayEquals(this.jsonNode, other);
  }

//  @Override
//  public int hashCode() {
//    int hashCode = 1;
//    for (int i = 0; i < this.size(); i++) {
//      hashCode = 31 * hashCode + this.get(i).hashCode();
//    }
//    return hashCode;
//  }
  
  final class JsonValueIterator implements ListIterator<JsonValue> {
    
    private int currentIndex;
    
    JsonValueIterator() {
      this(0);
    }
    
    JsonValueIterator(int initial) {
      this.currentIndex = initial;
    }

    @Override
    public boolean hasNext() {
      return this.currentIndex != jsonNode.size();
    }

    @Override
    public JsonValue next() {
      JsonValue jsonValue = get(this.currentIndex);
      this.currentIndex += 1;
      return jsonValue;
    }

    @Override
    public boolean hasPrevious() {
      return this.currentIndex != 0;
    }

    @Override
    public JsonValue previous() {
      JsonValue jsonValue = get(this.currentIndex - 1);
      this.currentIndex -= 1;
      return jsonValue;
    }

    @Override
    public int nextIndex() {
      return this.currentIndex;
    }

    @Override
    public int previousIndex() {
      return this.currentIndex - 1;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }

    @Override
    public void set(JsonValue e) {
      throw new UnsupportedOperationException("set");
    }

    @Override
    public void add(JsonValue e) {
      throw new UnsupportedOperationException("add");
    }
    
    @Override
    public void forEachRemaining(Consumer<? super JsonValue> action) {
      for (int i = this.currentIndex; i < size(); i++) {
        action.accept(get(i));
      }
      this.currentIndex = size();
    }
    
  }

}
