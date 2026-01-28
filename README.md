<!--
@guidance:
Content Structure:
# Project Title
- Extract the project title from `pom.xml` and display it as the main heading.
- Place `![](src/site/resources/images/ai-ae-plugin.png)` above the title.
- Directly below the title, add a Maven Central badge as a new paragraph:  
  `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])`  
  Replace `[groupId]` and `[artifactId]` with values from `pom.xml`.
# Overview
- Summarize the content of all `package-info.java` files in the source folders.
- Describe the application's core functionality in a professional, non-technical style. Do not mention package names.
- Provide a concise summary of the project's purpose and main features.
# Installation Instructions
- List all prerequisites, including the required Java version and build tools.
- Describe multiple installation and usage options:
  - **Clone and Build:**  
    - Provide step-by-step instructions for checking out the repository and building the project with Maven.
  - **Download Assembled Jar:**  
    - Include a direct download link for the application jar:  
      `[![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/anteater/files/plugins/ai-ae-plugin.jar/download)`
    - Instruct users to place the downloaded `.jar` file into the `plugins` folder of their Anteater installation.
  - **Launch Anteater Desktop or CLI:**  
    - Explain that the application will automatically detect and load the plugin.
  - **Maven Integration:**  
    - Show how to add the plugin as a dependency in the project’s `pom.xml`.
    - Explain that Maven will handle downloading and including the plugin during the build process.
# Usage
- Provide instructions for:
  - **Code Helpers:**  
    - Analyze class files in the `/src/main/java/com/ganteater/ae/desktop/editor` folder.
    - Describe their functionality.
    - Use `src/ae/ae.xml` to create usage examples.
  - **Processors:**  
    - Analyze class files in the `/src/main/java/com/ganteater/ae/processor` folder.
    - Describe their functionality.
    - Outline a typical workflow for using these components.
    - Use `src/ae/recipes` to create usage examples.
**Formatting Requirements:**
- Use Markdown syntax for all headings, lists, code blocks, and links.
- Ensure each section is clear, concise, and logically organized.
-->

![](src/site/resources/images/ai-ae-plugin.png)

# AI Anteater Plugin

[![Maven Central](https://img.shields.io/maven-central/v/com.ganteater.plugins/ai-ae-plugin.svg)](https://central.sonatype.com/artifact/com.ganteater.plugins/ai-ae-plugin)

## Overview
AI Anteater Plugin extends Anteater with AI-assisted authoring in the desktop editor and AI-driven automation in recipes.

In the desktop editor, it adds a guided prompt experience that uses the current document and other relevant context to request updates from an OpenAI-compatible service, then applies the returned changes directly into the editor.

In automation workflows, it provides recipe processors that can send prompts (including multi-message conversations), list available models, enable optional web-search tooling, and expose recipe tasks as callable tools/functions so a model can request structured actions and receive results.

## Installation Instructions
### Prerequisites
- Java 11
- Apache Maven
- Anteater (Desktop or CLI)
- AI provider credentials:
  - OpenAI-compatible API key (for example, OpenAI), or
  - CodeMie username/password

### Clone and Build
1. Clone the repository:

```bash
git clone https://github.com/ganteater/ai-ae-plugin.git
cd ai-ae-plugin
```

2. Build the plugin:

```bash
mvn -U clean package
```

3. (Optional) Build an assembled jar:

```bash
mvn -U clean package -Ppack
```

### Download Assembled Jar
[![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/anteater/files/plugins/ai-ae-plugin.jar/download)

1. Download the `.jar` file.
2. Copy it into the `plugins` folder of your Anteater installation.

### Launch Anteater Desktop or CLI
Start Anteater normally. It will automatically detect and load the plugin from the `plugins` directory.

### Maven Integration
Add the dependency to your project’s `pom.xml`:

```xml
<dependency>
  <groupId>com.ganteater.plugins</groupId>
  <artifactId>ai-ae-plugin</artifactId>
  <version><!-- set the desired version --></version>
</dependency>
```

Maven will download the artifact and include it in your build.

## Usage
### Code Helpers
The plugin integrates into the desktop recipe editor through editor helpers:
- **AICodeHelper** reads configuration (model, base URL, API key, debug flag), initializes an OpenAI-compatible client asynchronously, and exposes a small progress indicator.
- **AIHelperDialog** collects editor state (content, caret, selection) and environment context (available commands, views, and system variables), sends the combined request to the AI provider, and applies the returned recipe updates back into the editor.
- **CodeMieHelper** authenticates using a username/password flow to obtain a token and then uses that token when creating the OpenAI-compatible client.

Example configuration (from `src/ae/ae.xml`):

```xml
<Configuration name="OpenAI Demo">
  <Recipes path="recipes/openai"/>
  <Var name="OPENAI_API_KEY" init="console" type="password" />
  <Editor helper="AICodeHelper" apiKey="$var{OPENAI_API_KEY}" />
</Configuration>

<Configuration name="CodeMie Demo">
  <Recipes path="recipes/codemie"/>

  <Table name="CodeMie Credentials">
    <var name="CodeMie Username" />
    <var name="CodeMie Password" type="password" />
  </Table>

  <Editor helper="CodeMieHelper"
    username="$var{CodeMie Username}" password="$var{CodeMie Password}"
    model="gpt-5-2-2025-12-11" />
</Configuration>
```

### Processors
Processors are used from Anteater recipes via `<Extern>` to interact with an OpenAI-compatible provider.

Typical workflow:
1. Configure the provider in `<Extern>` (credentials, default model, optional base URL).
2. (Optional) Register callable tools/functions that map to recipe tasks.
3. Send a prompt (single text or multiple `<message>` entries).
4. Store and use the assistant response as a recipe variable.

Model listing example (from `src/ae/recipes/openai/OpenAI.recipe`):

```xml
<Extern class="OpenAI" apiKey="$var{OPENAI_API_KEY}" baseUrl="https://codemie.lab.epam.com/code-assistant-api/v1">
  <Models name="models" />
  <Var name="model" source="models" init="console" />
</Extern>
```

Prompt example (from `src/ae/recipes/openai/OpenAI.recipe`):

```xml
<Extern class="OpenAI" model="gpt-5-2-2025-12-11" apiKey="$var{OPENAI_API_KEY}" baseUrl="https://codemie.lab.epam.com/code-assistant-api/v1">
  <Prompt name="responseText">Write a short poem about the beauty of nature.</Prompt>
  <Out name="responseText" level="info" />
</Extern>
```

Function tool example (from `src/ae/recipes/openai/Function Tool Test.recipe`):

```xml
<Extern class="OpenAI" model="gpt-5-mini" apiKey="$var{OPENAI_API_KEY}">
  <Function name="get-weather" description="Get current weather for a city" type="object" return="weatherResult">
    <property name="city" type="string" required="true" />
    <Task>
      <Var name="weatherResult">{ "city": "$var{city}", "forecast": "sunny" }</Var>
      <Out name="city" level="info" />
      <Out name="weatherResult" level="info" />
    </Task>
  </Function>

  <Prompt name="openaiResponse">
    <message role="system">You are a helpful assistant that can call function get-weather when asked.</message>
    <message role="user">What is the weather in Paris?</message>
  </Prompt>

  <Out name="openaiResponse" level="info" />
</Extern>
```

Web search tool example (from `src/ae/recipes/openai/WebSearch Tool Test.recipe`):

```xml
<Extern class="CodeMie" username="$var{CODEMIE USERNAME}" password="$var{CODEMIE PASSWORD}">
  <Models name="models" />
  <Var name="models" init="console" />
  <WebSearch type="web_search_preview" />
  <Prompt name="response" model="$var{models}">What is it ganteater.com site?</Prompt>
  <Out name="response" />
</Extern>
```

CodeMie processor example (from `src/ae/recipes/codemie/CodeMie Test.recipe`):

```xml
<Var name="CodeMie Username" init="mandatory" />
<Var name="CodeMie Password" init="mandatory" type="password" />
<Var name="User Prompt" init="mandatory" />

<Extern class="CodeMie" username="$var{CodeMie Username}" password="$var{CodeMie Password}" model="gpt-4o-2024-11-20">
  <Prompt name="CodeMie Response">
    <message role="system">You are an assistant. If web page content is provided, review it and answer accordingly.</message>
    <message role="user">User request: $var{User Prompt}</message>
  </Prompt>
</Extern>

<View name="codeMieResponseView" reuse="false" type="Markdown" />
<Out view="codeMieResponseView">$var{CodeMie Response}</Out>
```
