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
 * 
 * @see org.eclipse.parsson.JsonStructureParser
 */
public final class JsonNodeJsonParser implements JsonParser {

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

  public JsonNodeJsonParser(JsonNode root) {
    Objects.requireNonNull(root, "root");
    this.currentNode = JsonNodeIterator.adapt(root);
    this.nodeStack = new ArrayDeque<>();
  }

  @Override
  public boolean hasNext() {
//    if (this.currentState == Event.END_OBJECT || this.currentState == Event.END_ARRAY) {
//      return this.nodeStack.isEmpty();
//    }
//    return true;
    return !((this.currentState == Event.END_OBJECT || this.currentState == Event.END_ARRAY) && this.nodeStack.isEmpty());
  }

  @Override
  public Event next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    transition();
    return this.currentState;
  }

  private void transition() {
    if (this.currentState == null) {
      this.currentState = this.currentNode.startEvent();
    } else {
      if (this.currentState == Event.END_OBJECT || this.currentState == Event.END_ARRAY) {
        this.currentNode = this.nodeStack.pop();
      }
      switch (this.currentNode) {
        case ArrayJsonNodeIterator currentArray -> {
          if (currentArray.hasNext()) {
            currentArray.next();
            nextStateAndEndOfTheObjectOrArray();
          } else {
            this.currentState = Event.END_ARRAY;
          }
        }
        case ObjectJsonNodeIterator currentObject -> {
          if (this.currentState == Event.KEY_NAME) {
            nextStateAndEndOfTheObjectOrArray();
          } else {
            if (currentObject.hasNext()) {
              currentObject.next();
              this.currentState = Event.KEY_NAME;
            } else {
              this.currentState = Event.END_OBJECT;
            }
          }
        }
      };
    }
  }

  private void nextStateAndEndOfTheObjectOrArray() {
    this.currentState = getState(this.currentNode.getJsonValue());
    if (this.currentState == Event.START_ARRAY || this.currentState == Event.START_OBJECT) {
      this.nodeStack.push(this.currentNode);
      this.currentNode = JsonNodeIterator.adapt(this.currentNode.getJsonValue());
    }
//    if (this.currentState == Event.START_ARRAY) {
//      this.nodeStack.push(this.currentNode);
//      this.currentNode = new ArrayJsonNodeIterator(this.currentNode.getJsonValue());
//    } else if (this.currentState == Event.START_OBJECT) {
//      this.nodeStack.push(this.currentNode);
//      this.currentNode = new ObjectJsonNodeIterator(this.currentNode.getJsonValue());
//    }
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
    // TODO Auto-generated method stub
    return JsonParser.super.getObject();
  }

  @Override
  public JsonValue getValue() {
    // TODO Auto-generated method stub
    return JsonParser.super.getValue();
  }

  @Override
  public JsonArray getArray() {
    // TODO Auto-generated method stub
    return JsonParser.super.getArray();
  }

  @Override
  public Stream<JsonValue> getArrayStream() {
    // TODO Auto-generated method stub
    return JsonParser.super.getArrayStream();
  }

  @Override
  public Stream<Entry<String, JsonValue>> getObjectStream() {
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
    // TODO Auto-generated method stub
    JsonParser.super.skipArray();
  }

  @Override
  public void skipObject() {
    // TODO Auto-generated method stub
    JsonParser.super.skipObject();
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

    boolean hasNext();

    Object next();

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
      public boolean hasNext() {
        return this.iterator.hasNext();
      }

      @Override
      public Entry<String, JsonNode> next() {
        Map.Entry<String, JsonNode> next = this.iterator.next();
        this.key = next.getKey();
        this.value = next.getValue();
        return next;
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
      public boolean hasNext() {
        return this.nodeIterator.hasNext();
      }

      @Override
      public JsonNode next() {
        this.value = this.nodeIterator.next();
        return this.value;
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
