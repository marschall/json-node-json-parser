package com.github.marschall.jsonnodereader;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.json.JsonNumber;

final class JsonNodeJsonNumber implements JsonNumber {
  
  private final JsonNode jsonNode;

  JsonNodeJsonNumber(JsonNode jsonNode) {
    Objects.requireNonNull(jsonNode, "jsonNode");
    this.jsonNode = jsonNode;
  }

  @Override
  public ValueType getValueType() {
    return ValueType.NUMBER;
  }

  @Override
  public boolean isIntegral() {
    return this.jsonNode.isIntegralNumber();
  }

  @Override
  public int intValue() {
    return this.jsonNode.intValue();
  }

  @Override
  public int intValueExact() {
    if (this.jsonNode.canConvertToInt()) {
      return this.jsonNode.intValue();
    } else {
      throw new ArithmeticException();
    }
  }

  @Override
  public long longValue() {
    return this.jsonNode.longValue();
  }

  @Override
  public long longValueExact() {
    if (this.jsonNode.canConvertToLong()) {
      return this.jsonNode.longValue();
    } else {
      throw new ArithmeticException();
    }
  }

  @Override
  public BigInteger bigIntegerValue() {
    return this.jsonNode.bigIntegerValue();
  }

  @Override
  public BigInteger bigIntegerValueExact() {
    if (this.jsonNode.canConvertToExactIntegral()) {
      return this.jsonNode.bigIntegerValue();
    } else {
      throw new ArithmeticException();
    }
  }

  @Override
  public double doubleValue() {
    return this.jsonNode.doubleValue();
  }

  @Override
  public BigDecimal bigDecimalValue() {
    return this.jsonNode.decimalValue();
  }

}
