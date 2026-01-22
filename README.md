![](src/site/resources/images/ai-ae-plugin.png)

# AI Anteater Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)

## Overview

AI Anteater Plugin is an Anteater (AE) plugin that integrates OpenAI/OpenAI-compatible services into AE recipes via custom processors.

## Introduction

This module adds AE processors that let you call AI models from within an AE recipe:

- **`OpenAI` processor**: sends prompts (one or more `<message>` items) to the OpenAI Responses API and stores the resulting text into an AE property.
- **Function tools (`<Function>`)**: declare JSON-schema-like parameters and allow the model to request tool execution; the plugin runs the nested AE `<Task>` and returns its output to the model as a function-call result.
- **Model listing (`<Models>`)**: retrieves available model IDs and stores them into an AE property.
- **Web search tool (`<WebSearch>`)**: registers a web-search tool (optionally with approximate user location) for model calls.
- **`CodeMie` processor**: obtains an access token from CodeMie credentials and then uses an OpenAI-compatible workflow.

## Prerequisites

### Java and Maven

- **Java 9** (per `pom.xml` `java.version`)
- Maven running on a JDK that supports Java 9

### Build and dependencies

This module pulls dependencies via Maven, including:

- `com.openai:openai-java` (OpenAI Java SDK)

Build the project:

```bash
mvn -q -DskipTests package
```

### AE integration

This module is intended to be used as an AE plugin and configured from an AE recipe using `<Extern class='...'>`.

### Credentials

#### OpenAI (or OpenAI-compatible)

Provide:

- `apiKey` (required)
- `baseUrl` (optional; for OpenAI-compatible endpoints)
- `model` (optional; defaults to `gpt-5-mini`)

Example:

```xml
<Extern class='OpenAI' apiKey='${OPENAI_API_KEY}' model='gpt-5-mini' baseUrl='https://api.openai.com/v1'/>
```

#### CodeMie (optional)

Provide:

- `username`
- `password`

Example:

```xml
<Extern class='CodeMie' username='${CODEMIE_USERNAME}' password='${CODEMIE_PASSWORD}'/>
```

## Basic usage

### Prompt

`<Prompt>` collects one or more messages and writes the resulting response text into the AE property specified by `name`:

```xml
<Prompt name='ai.result'>
  <message role='user'>Summarize: ${text}</message>
</Prompt>
```

### Function tool + Task

Declare a tool the model can call, map its arguments into AE properties, execute an AE `<Task>`, and return a value back to the model:

```xml
<Function name='make_note' description='Create a note' type='object' return='noteId'>
  <property name='title' type='string' required='true'/>
  <property name='body' type='string' required='true'/>
  <Task>
    <!-- implement your AE automation here -->
    <Var name='noteId' value='12345'/>
  </Task>
</Function>
```

### WebSearch tool

```xml
<WebSearch type='web_search_preview_2025_03_11' city='Kyiv' country='UA' region='Kyiv'/>
```

### Models

```xml
<Models name='ai.models'/>
```

## License

Apache License 2.0. See [LICENSE.txt](LICENSE.txt).
