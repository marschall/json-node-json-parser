package com.github.marschall.jsonnodereader;

import java.util.Objects;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.json.JsonString;

final class JsonNodeJsonString implements JsonString {

  private final JsonNode jsonNode;

  JsonNodeJsonString(JsonNode jsonNode) {
    Objects.requireNonNull(jsonNode, "jsonNode");
    this.jsonNode = jsonNode;
  }

  @Override
  public ValueType getValueType() {
    return ValueType.STRING;
  }

  @Override
  public String getString() {
    return this.jsonNode.textValue();
  }

  @Override
  public CharSequence getChars() {
    return this.getString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof JsonString other)) {
      return false;
    }
    return this.getString().equals(other.getString());
  }

  @Override
  public int hashCode() {
    return this.jsonNode.hashCode();
  }

  @Override
  public String toString() {
    String textValue = this.jsonNode.textValue();
    StringBuilder output = new StringBuilder(textValue.length() + 2);
    output.append('"');
    JsonStringEncoder.getInstance().quoteAsString(textValue, output);
    output.append('"');
    return output.toString();
  }

}
