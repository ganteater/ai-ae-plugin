![](src/site/resources/images/ai-ae-plugin.png)

# AI Anteater Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)

## Overview

AI Anteater Plugin is an Anteater (AE) plugin that adds AI-assisted capabilities to AE recipes and the AE Desktop editor.

It provides:

- An **OpenAI-backed recipe processor** that can send prompts (messages) to OpenAI-compatible APIs and store the response into AE properties.
- Support for **tool/function calling** from the model back into AE recipes via `<Function>` + `<Task>`.
- A **Web Search tool** configuration for models that support web search tools.
- An **AE Desktop editor helper dialog** that builds a structured prompt from the current editor content (including caret/selection markers) plus context about available AE processors, views, and system variables, and then applies the model output back into the editor.

## Introduction

This module integrates AI into the Anteater ecosystem in two ways:

1. **At runtime (recipes):** the `OpenAI` processor accepts a `<Prompt>` command with one or more `<message>` blocks (role-based), submits them via the OpenAI Java SDK `responses` API, and stores the returned output text into a named AE property. It can also register tools such as `<Function>` (implemented as function tools) and `<WebSearch>` (implemented as web search tools). When the model requests a function call, the processor maps the call to the matching `<Function>` node, copies arguments into AE variables, executes the function’s `<Task>` nodes, and returns the produced value back to the model.

2. **In the desktop editor:** `AICodeHelper` and `AIHelperDialog` assemble a prompt that includes the current document source with special markers for cursor/selection (`Marker.CURSOR`, `Marker.SELECTION_START`, `Marker.SELECTION_END`) via `Prompt`, plus extra context such as processor command descriptions/examples and system variable names. The helper then sends the request to the configured model and updates the editor content with the response.

## Prerequisites

### 1) Java

- Java **9** (as configured by `pom.xml` `java.version=9`).

### 2) Maven

- Maven project; build with standard Maven lifecycle.

### 3) OpenAI Java SDK dependency

This module depends on:

- `com.openai:openai-java:4.8.0`

### 4) API access (required)

You must provide an API key for OpenAI or an OpenAI-compatible provider.

In AE configuration/recipe XML, the `OpenAI` processor initialization requires:

- `apiKey` (required)
- `model` (optional, defaults to `gpt-5-mini`)
- `baseUrl` (optional; set this for OpenAI-compatible endpoints)

Example:

```xml
<Extern class="OpenAI" model="gpt-5-mini" apiKey="${OPENAI_API_KEY}" baseUrl="https://api.openai.com/v1" />
```

### 5) Optional: CodeMie credentials (if using CodeMie)

If you use the `CodeMie` processor, initialization requires:

- `username`
- `password`

It will exchange them for a token and then configure `apiKey`/`baseUrl` internally.

Example:

```xml
<Extern class="CodeMie" username="${CODEMIE_USERNAME}" password="${CODEMIE_PASSWORD}" />
```
