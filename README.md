JsonNodeJsonParser [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.marschall/json-node-json-parser/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.marschall/json-node-json-parser) [![Javadocs](https://www.javadoc.io/badge/com.github.marschall/json-node-json-parser.svg)](https://www.javadoc.io/doc/com.github.marschall/json-node-json-parser)
==================

A [Jakarta](https://jakarta.ee/specifications/jsonp/) `JsonParser` that operates on a [Jackson](https://github.com/FasterXML/jackson) `JsonNode`.

Usage
-----

```xml
<dependency>
  <groupId>com.github.marschall</groupId>
  <artifactId>json-node-json-parser</artifactId>
  <version>1.0.0</version>
</dependency>
```

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
