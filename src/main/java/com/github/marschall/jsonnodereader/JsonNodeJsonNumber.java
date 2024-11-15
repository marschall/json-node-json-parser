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
    if (this.jsonNode.canConvertToInt() && this.jsonNode.canConvertToExactIntegral()) {
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
    if (this.jsonNode.canConvertToLong() && this.jsonNode.canConvertToExactIntegral()) {
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
  
  @Override
  public Number numberValue() {
    return this.jsonNode.numberValue();
  }
  
  @Override
  public int hashCode() {
    // BigDecimal allocation could be avoided for int/log
    return bigDecimalValue().hashCode();
  }
  
  @Override
  public String toString() {
    // BigDecimal allocation could be avoided for int/log
    return bigDecimalValue().toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof JsonNumber other)) {
      return false;
    }
    return this.bigDecimalValue().equals(other.bigDecimalValue());
  }

}
