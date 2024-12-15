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

Very similar to `JsonParserFactory#createParser(JsonObject)` but working on a Jackson instead of a JSON-P node.

```java
JsonObject jsonObject = ;
try (JsonParser parser = Json.createParserFactory(null).createParser(jsonObject)) {
  // 
}
```
