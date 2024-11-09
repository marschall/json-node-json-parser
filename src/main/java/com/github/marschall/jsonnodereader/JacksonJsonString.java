package com.github.marschall.jsonnodereader;

import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.json.JsonString;

final class JacksonJsonString implements JsonString {

  private final JsonNode jsonNode;

  JacksonJsonString(JsonNode jsonNode) {
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

}