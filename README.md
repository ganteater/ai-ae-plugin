![](src/site/resources/images/ai-ae-plugin.png)

# AI Anteater Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)

## Overview

AI Anteater Plugin provides Anteater (AE) processors and desktop tooling that let AE recipes call OpenAI/OpenAI-compatible chat models, optionally expose AE Tasks as callable “function tools”, and persist results back into AE variables/properties.

## Introduction

This module adds AI-oriented processors that can be referenced from AE recipes via `<Extern class="...">` and then used as regular AE commands:

- **`OpenAI` processor**: executes OpenAI-compatible chat-completions using a list of `<message>` items and writes the final text response into an AE property.
- **`CodeMie` processor**: authenticates against a CodeMie server and performs OpenAI-compatible model calls.
- **Tools for model calls**:
  - **Function tools (`<Function>`)**: declare a tool name/description and input properties (JSON-schema-like). When the model requests a tool call, the plugin runs the nested AE `<Task>` and returns its output back to the model.
  - **`<WebSearch>` tool**: registers a web-search tool for model calls (optionally with approximate user location fields).
- **`<Models>`**: requests available model IDs and stores them into an AE property.

## Prerequisites

### Java

- **Java 9** (per `pom.xml` `java.version`). Ensure your `JAVA_HOME` points to a JDK 9+ installation.

### Maven

- Use Maven with a JDK that supports Java 9.

### Dependency resolution

Building requires downloading dependencies from Maven repositories, including:

- `com.openai:openai-java:4.8.0`

Build:

```bash
mvn -q -DskipTests package
```

### Using as an Anteater (AE) plugin

1. Build or install the plugin artifact.
2. Configure it in your AE runtime/recipe using `<Extern class="...">` and then call the provided commands in your recipe.

### Credentials

#### OpenAI (or OpenAI-compatible)

Provide:

- `apiKey` (required)
- `baseUrl` (optional; for OpenAI-compatible endpoints)
- `model` (optional; defaults depend on processor configuration)

Example:

```xml
<Extern class="OpenAI" apiKey="$var{OPENAI_API_KEY}" model="gpt-5-mini" baseUrl="https://api.openai.com/v1"/>
```

#### CodeMie (optional)

Provide:

- `username`
- `password`

Example:

```xml
<Extern class="CodeMie" username="$var{CODEMIE_USERNAME}" password="$var{CODEMIE_PASSWORD}"/>
```

## Basic usage

### Prompt

`<Prompt>` collects one or more messages and writes the resulting response text into the AE property specified by `name`:

```xml
<Prompt name="ai.result">
  <message role="user">Summarize: $var{text}</message>
</Prompt>
```

### Function tool + Task

Declare a tool the model can call, map its arguments into AE properties, execute an AE `<Task>`, and return a value back to the model:

```xml
<Function name="make_note" description="Create a note" type="object" return="noteId">
  <property name="title" type="string" required="true"/>
  <property name="body" type="string" required="true"/>
  <Task>
    <!-- implement your AE automation here -->
    <Var name="noteId" value="12345"/>
  </Task>
</Function>
```

### WebSearch tool

```xml
<WebSearch type="web_search_preview_2025_03_11" city="Kyiv" country="UA" region="Kyiv"/>
```

### Models

```xml
<Models name="ai.models"/>
```

## License

Apache License 2.0. See [LICENSE.txt](LICENSE.txt).
