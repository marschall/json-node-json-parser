JsonNodeJsonParser
==================

A [Jakarta](https://jakarta.ee/specifications/jsonp/) `JsonParser` that operates on a [Jackson](https://github.com/FasterXML/jackson) `JsonNode`.

Usage
-----

```java
JsonNode jacksonNode = ;
try (JsonParser parser = new JsonNodeJsonParser(jacksonNode)) {
  // 
}
```
