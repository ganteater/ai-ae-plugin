![](src/site/resources/images/ai-ae-plugin.png)

# AI Anteater Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)

## Overview

AI Anteater Plugin is an Anteater (AE) plugin that adds AI processors and desktop editor helpers. It lets AE recipes call OpenAI/OpenAI-compatible chat models, optionally register AE Tasks as callable function tools, run those Tasks when the model requests a tool call, and store responses back into AE variables/properties.

## Introduction

The plugin provides two AE processors (registered via `META-INF/services/com.ganteater.ae.processor.Processor`) and desktop UI helpers:

- **`OpenAI` processor** (`com.ganteater.ae.processor.OpenAI`):
  - Builds a chat request from `<message>` elements (role/content).
  - Sends the request using `com.openai:openai-java`.
  - Supports tools (functions and web search) and writes the final model output into an AE property.

- **`CodeMie` processor** (`com.ganteater.ae.processor.CodeMie`):
  - Uses credentials to obtain an access token from a CodeMie server.
  - Performs OpenAI-compatible chat requests against that server.
  - Supports the same tool mechanism and result persistence.

- **Tool integration** (implemented in `AbstractAIProcessor`):
  - **Function tools**: declare a tool schema and a nested AE `<Task>` to execute when the model calls the tool; the Task output is returned to the model.
  - **Web search tool**: optionally registers a web-search tool (with location fields) for model calls.

- **Desktop editor helpers** (`com.ganteater.ae.desktop.editor.*`): provide UI assistance for authoring prompts and working with AI-backed helpers inside the AE desktop environment.

## Prerequisites

### Java

- **Java 9+** (per `pom.xml` `java.version`). Ensure `JAVA_HOME` points to a JDK 9+.

### Maven

- Use Maven with a Java 9+ toolchain.

Build:

```bash
mvn -q -DskipTests package
```

### Dependencies

The plugin depends on:

- `com.openai:openai-java:4.8.0`

### Using it in Anteater (AE)

1. Build the plugin JAR:

   ```bash
   mvn -q -DskipTests package
   ```

2. Add the produced plugin JAR to your AE runtime/plugins (the exact folder depends on your AE installation).
3. In your AE recipe, register the processor via `<Extern .../>` and then use the processor commands.

### Credentials and configuration

#### OpenAI (or OpenAI-compatible endpoint)

Provide:

- `apiKey` (required)
- `baseUrl` (optional; for OpenAI-compatible APIs)
- `model` (recommended)

Example:

```xml
<Extern class="OpenAI" apiKey="$var{OPENAI_API_KEY}" model="gpt-5-mini" baseUrl="https://api.openai.com/v1"/>
```

#### CodeMie

Provide:

- `username`
- `password`

Example:

```xml
<Extern class="CodeMie" username="$var{CODEMIE_USERNAME}" password="$var{CODEMIE_PASSWORD}"/>
```

## License

Apache License 2.0. See [LICENSE.txt](LICENSE.txt).
