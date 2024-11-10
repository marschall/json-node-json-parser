package com.github.marschall.jsonnodereader;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.github.marschall.jsonnodereader.JsonNodeJsonParser.JsonNodeIterator.ArrayJsonNodeIterator;
import com.github.marschall.jsonnodereader.JsonNodeJsonParser.JsonNodeIterator.ObjectJsonNodeIterator;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;

/**
 * A Jakarta {@link JsonParser} that works on a Jackson {@link JsonNode}.
 */
public final class JsonNodeJsonParser implements JsonParser {
  // implementation similar to org.eclipse.parsson.JsonStructureParser

  private static final Map<JsonNodeType, Event> TYPE_TO_EVENT_MAP;

  static {
    TYPE_TO_EVENT_MAP = new EnumMap<>(JsonNodeType.class);
    TYPE_TO_EVENT_MAP.put(JsonNodeType.ARRAY, Event.START_ARRAY);
    TYPE_TO_EVENT_MAP.put(JsonNodeType.OBJECT, Event.START_OBJECT);
    TYPE_TO_EVENT_MAP.put(JsonNodeType.NUMBER, Event.VALUE_NUMBER);
    TYPE_TO_EVENT_MAP.put(JsonNodeType.STRING, Event.VALUE_STRING);
    TYPE_TO_EVENT_MAP.put(JsonNodeType.NULL, Event.VALUE_NULL);
  }

  private static Event getState(JsonNode node) {
    JsonNodeType nodeType = node.getNodeType();
    if (nodeType == JsonNodeType.BOOLEAN) {
      return node.booleanValue() ? Event.VALUE_TRUE : Event.VALUE_FALSE;
    }
    Event state = TYPE_TO_EVENT_MAP.get(nodeType);
    if (state == null) {
      throw new IllegalStateException("unsupported node type");
    } else {
      return state;
    }
  }

  private JsonNodeIterator currentNode;
  private Event currentState;
  private final Deque<JsonNodeIterator> nodeStack;

  /**
   * Initializes a {@link JsonNodeJsonParser}.
   * 
   * @param root the root node, must be an array or object, not {@code null}
   */
  public JsonNodeJsonParser(JsonNode root) {
    Objects.requireNonNull(root, "root");
    this.currentNode = JsonNodeIterator.adapt(root);
    this.nodeStack = new ArrayDeque<>();
  }

  @Override
  public boolean hasNext() {
    if (this.currentState == Event.END_OBJECT || this.currentState == Event.END_ARRAY) {
      return !this.nodeStack.isEmpty();
    }
    return true;
  }

  @Override
  public Event next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    advance();
    return this.currentState;
  }

  private void advance() {
    if (this.currentState == null) {
      this.currentState = this.currentNode.startEvent();
    } else {
      if (this.currentState == Event.END_OBJECT || this.currentState == Event.END_ARRAY) {
        this.currentNode = this.nodeStack.pop();
      }
      this.currentState = this.currentNode.nextState(this.currentState);
      this.pushNodeIfStart();
    }
  }
  
  private void pushNodeIfStart() {
    if (this.currentState == Event.START_ARRAY) {
      this.nodeStack.push(this.currentNode);
      this.currentNode = new ArrayJsonNodeIterator(this.currentNode.getJsonValue());
    } else if (this.currentState == Event.START_OBJECT) {
      this.nodeStack.push(this.currentNode);
      this.currentNode = new ObjectJsonNodeIterator(this.currentNode.getJsonValue());
    }
  }

  @Override
  public String getString() {
    return switch (this.currentState) {
      case KEY_NAME -> ((ObjectJsonNodeIterator) this.currentNode).getKey();
      case VALUE_STRING, VALUE_NUMBER -> this.currentNode.getJsonValue().asText();
      default -> throw new IllegalStateException("getString() not supported in current state");
    };
  }

  @Override
  public boolean isIntegralNumber() {
    if (this.currentState != Event.VALUE_NUMBER) {
      throw new IllegalStateException("current state is not a number");
    }
    return this.currentNode.getJsonValue().isIntegralNumber();
  }

  @Override
  public int getInt() {
    if (this.currentState != Event.VALUE_NUMBER) {
      throw new IllegalStateException("current state is not a number");
    }
    return this.currentNode.getJsonValue().intValue();
  }

  @Override
  public long getLong() {
    if (this.currentState != Event.VALUE_NUMBER) {
      throw new IllegalStateException("current state is not a number");
    }
    return this.currentNode.getJsonValue().longValue();
  }

  @Override
  public BigDecimal getBigDecimal() {
    if (this.currentState != Event.VALUE_NUMBER) {
      throw new IllegalStateException("current state is not a number");
    }
    return this.currentNode.getJsonValue().decimalValue();
  }

  @Override
  public JsonLocation getLocation() {
    return LocationUnkown.INSTANCE;
  }

  @Override
  public Event currentEvent() {
    return this.currentState;
  }

  @Override
  public JsonObject getObject() {
    if (this.currentState == Event.START_OBJECT) {
      throw new IllegalStateException("not in start object");
    }
    JsonObject object;
    JsonNode currentValue = this.currentNode.getJsonValue();
    if (currentValue.isEmpty()) {
      object = JsonValue.EMPTY_JSON_OBJECT;
    } else {
      object = new JsonNodeJsonObject(currentValue);
    }
    // #transition() will pop the stack 
    this.currentState = Event.END_OBJECT;
    return object;
  }

  @Override
  public JsonValue getValue() {
    return switch (this.currentState) {
      case END_OBJECT, END_ARRAY -> throw new IllegalStateException("in state end");
      case START_ARRAY -> this.getArray();
      case START_OBJECT -> this.getObject();
      case KEY_NAME, VALUE_STRING -> new JsonNodeJsonString(this.currentNode.getJsonValue());
      case VALUE_NUMBER -> new JsonNodeJsonNumber(this.currentNode.getJsonValue());
      case VALUE_TRUE -> JsonValue.TRUE;
      case VALUE_FALSE -> JsonValue.FALSE;
      case VALUE_NULL -> JsonValue.NULL;
    };
  }

  @Override
  public JsonArray getArray() {
    if (this.currentState == Event.START_ARRAY) {
      throw new IllegalStateException("not in start array");
    }
    JsonArray array;
    JsonNode currentValue = this.currentNode.getJsonValue();
    if (currentValue.isEmpty()) {
      array = JsonValue.EMPTY_JSON_ARRAY;
    } else {
      array = new JsonNodeJsonArray(currentValue);
    }
    // #transition() will pop the stack 
    this.currentState = Event.END_ARRAY;
    return array;
  }

  @Override
  public Stream<JsonValue> getArrayStream() {
    if (this.currentState == Event.START_ARRAY) {
      throw new IllegalStateException("not in start array");
    }
    // TODO Auto-generated method stub
    return JsonParser.super.getArrayStream();
  }

  @Override
  public Stream<Entry<String, JsonValue>> getObjectStream() {
    if (this.currentState == Event.START_OBJECT) {
      throw new IllegalStateException("not in start object");
    }
    // TODO Auto-generated method stub
    return JsonParser.super.getObjectStream();
  }

  @Override
  public Stream<JsonValue> getValueStream() {
    // TODO Auto-generated method stub
    return JsonParser.super.getValueStream();
  }

  @Override
  public void skipArray() {
    if (this.currentNode instanceof ArrayJsonNodeIterator a) {
      this.currentState = Event.END_ARRAY;
      // #transition() will pop the stack 
    }
  }

  @Override
  public void skipObject() {
    if (this.currentNode instanceof ObjectJsonNodeIterator o) {
      this.currentState = Event.END_OBJECT;
      // #transition() will pop the stack 
    }
  }

  @Override
  public void close() {
    // no-op for now

  }

  sealed interface JsonNodeIterator {

    static JsonNodeIterator adapt(JsonNode jsonNode) {
      if (jsonNode.isObject()) {
        return new ObjectJsonNodeIterator(jsonNode);
      } else if (jsonNode.isArray()) {
        return new ArrayJsonNodeIterator(jsonNode);
      } else {
        throw new IllegalArgumentException("unsupported node type");
      }
    }

    Event nextState(Event currentState);

    JsonNode getJsonValue();
    
    Event startEvent();

    static final class ObjectJsonNodeIterator implements JsonNodeIterator {

      private final Iterator<Entry<String, JsonNode>> iterator;
      private JsonNode value;
      private String key;

      ObjectJsonNodeIterator(JsonNode object) {
        this.iterator = object.fields();
      }
      
      @Override
      public Event startEvent() {
        return Event.START_OBJECT;
      }

      String getKey() {
        return this.key;
      }

      @Override
      public Event nextState(Event currentState) {
        if (currentState == Event.KEY_NAME) {
          return getState(this.value);
        } else  if (this.iterator.hasNext()) {
          Map.Entry<String, JsonNode> next = this.iterator.next();
          this.key = next.getKey();
          this.value = next.getValue();
          return Event.KEY_NAME;
        } else {
          return Event.END_OBJECT;
        }
      }

      @Override
      public JsonNode getJsonValue() {
        return this.value;
      }

    }

    static final class ArrayJsonNodeIterator implements JsonNodeIterator {

      private final Iterator<JsonNode> nodeIterator;
      private JsonNode value;

      ArrayJsonNodeIterator(JsonNode array) {
        this.nodeIterator = array.elements();
      }
      
      @Override
      public Event startEvent() {
        return Event.START_ARRAY;
      }

      @Override
      public Event nextState(Event currentState) {
        if (this.nodeIterator.hasNext()) {
          this.value = this.nodeIterator.next();
          return getState(this.value);
        } else {
          return Event.END_ARRAY;
        }
      }

      @Override
      public JsonNode getJsonValue() {
        return this.value;
      }

    }

  }

  static final class LocationUnkown implements JsonLocation {

    static final JsonLocation INSTANCE = new LocationUnkown();

    private LocationUnkown() {
      super();
    }

    @Override
    public long getLineNumber() {
      return -1L;
    }

    @Override
    public long getColumnNumber() {
      return -1L;
    }

    @Override
    public long getStreamOffset() {
      return -1L;
    }

    @Override
    public String toString() {
      return "(line no=-1, column no=-1, offset=-1)";
    }
  }

}
