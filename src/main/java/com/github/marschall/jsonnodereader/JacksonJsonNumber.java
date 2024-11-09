package com.github.marschall.jsonnodereader;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.json.JsonNumber;

final class JacksonJsonNumber implements JsonNumber {
  
  private final JsonNode jsonNode;

  JacksonJsonNumber(JsonNode jsonNode) {
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
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long longValue() {
    return this.jsonNode.longValue();
  }

  @Override
  public long longValueExact() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public BigInteger bigIntegerValue() {
    return this.jsonNode.bigIntegerValue();
  }

  @Override
  public BigInteger bigIntegerValueExact() {
    // TODO Auto-generated method stub
    return null;
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
