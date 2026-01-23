![](src/site/resources/images/ai-ae-plugin.png)

# AI Anteater Plugin

[![Maven Central](https://img.shields.io/maven-central/v/com.ganteater.plugins/ai-ae-plugin.svg)](https://central.sonatype.com/artifact/com.ganteater.plugins/ai-ae-plugin)

## Project Overview

AI Anteater Plugin is an Anteater (AE) plugin that adds AI-powered processors and desktop editor helpers. It lets AE recipes call OpenAI/OpenAI-compatible chat models (including a CodeMie OpenAI-compatible endpoint), optionally expose AE Tasks as callable function tools, execute those Tasks when the model requests a tool call, and store results back into AE variables/properties.

## Introduction

The module provides two processors (registered via `META-INF/services/com.ganteater.ae.processor.Processor`) that can be used from AE recipes:

- **`OpenAI`** (`com.ganteater.ae.processor.OpenAI`): sends chat requests via the `com.openai:openai-java` client using configured model/base URL/API key.
- **`CodeMie`** (`com.ganteater.ae.processor.CodeMie`): obtains an access token from a CodeMie server (username/password) and performs OpenAI-compatible chat requests.

Both processors share common behavior from `AbstractAIProcessor`:

- Build a prompt from `<message role="...">...</message>` items.
- Support **tools**:
  - **Function tools**: declare a tool schema and associate it with a nested AE `<Task>` that is executed when the model calls the tool; the Task result is returned back to the model.
  - Optional **web search** tool support for model tool calls.
- Persist the final model response into AE properties/variables for downstream recipe steps.

The plugin also includes desktop UI helpers under `com.ganteater.ae.desktop.editor.*` to assist with AI-backed prompt/code authoring in the AE desktop environment.

## Prerequisites

### Java

- **JDK 9+** (per `pom.xml` `java.version=9`). Ensure `JAVA_HOME` points to a JDK 9+ installation.

### Maven

Build the project:

```bash
mvn -q -DskipTests package
```

### Runtime dependencies

This plugin uses:

- `com.openai:openai-java:4.8.0`

### Using the plugin in Anteater (AE)

1. Build the plugin JAR:

   ```bash
   mvn -q -DskipTests package
   ```

2. Add the produced JAR to your AE installation’s plugin location (folder/name depends on your AE distribution).
3. Reference the processor from an AE recipe (examples below).

### Credentials and configuration

#### OpenAI (or OpenAI-compatible endpoint)

Configuration typically includes:

- `apiKey` (required)
- `model` (recommended)
- `baseUrl` (optional; for OpenAI-compatible APIs)

Example:

```xml
<Extern class="OpenAI" apiKey="$var{OPENAI_API_KEY}" model="gpt-5-mini" baseUrl="https://api.openai.com/v1"/>
```

#### CodeMie

Configuration typically includes:

- `username`
- `password`

Example:

```xml
<Extern class="CodeMie" username="$var{CODEMIE_USERNAME}" password="$var{CODEMIE_PASSWORD}"/>
```

## License

Apache License 2.0. See [LICENSE.txt](LICENSE.txt).
