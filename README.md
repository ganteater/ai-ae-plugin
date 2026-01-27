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
This plugin adds AI-powered assistance to Anteater.

It includes:
- Desktop editor helpers that can draft, refine, and fix Anteater recipes using an OpenAI-compatible chat model.
- Recipe processors that connect to OpenAI-compatible providers so recipes can prompt models, list available models, register tasks as callable tools, and (when supported) use web search.

## Installation Instructions
### Prerequisites
- Java 9
- Apache Maven
- Anteater (Desktop or CLI)
- Provider credentials (for example, an OpenAI API key or CodeMie username/password)

### Clone and Build
1. Clone this repository.
2. Build the project:

```bash
mvn -U clean package
```

3. (Optional) Build an assembled jar (if configured):

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
The desktop editor integration provides a dialog-driven assistant that gathers the current recipe text, caret/selection, and relevant environment context, then suggests updated recipe content and can apply it back into the editor.

Key components:
- **AICodeHelper**: Entry point used by the desktop editor. Reads configuration (for example, model, debug, apiKey/baseUrl), initializes an OpenAI-compatible client, and opens the helper dialog.
- **AIHelperDialog**: Builds the request context (system variables, available processors and views, static instruction resources, current editor state), sends it to the model, and applies the returned recipe text (optionally adjusting caret/selection).
- **CodeMieHelper**: Alternative helper that authenticates with CodeMie using username/password to obtain an access token, then creates the OpenAI-compatible client.

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
The processors are intended to be referenced from Anteater recipes via `<Extern>`, providing recipe-level commands for interacting with an AI provider.

Typical workflow:
1. Configure a provider in `<Extern>` (credentials, model, optional base URL).
2. (Optional) Register tools/functions so the model can call into recipe tasks.
3. Prompt the model and store the response into a variable.
4. Use recipe commands to output or further process the response.

OpenAI processor example (based on `src/ae/recipes/openai/OpenAI.recipe`):

```xml
<Extern class="OpenAI" apiKey="$var{OPENAI_API_KEY}" baseUrl="https://codemie.lab.epam.com/code-assistant-api/v1">
  <Models name="models" />
  <Var name="model" source="models" init="console" />
</Extern>

<Extern class="OpenAI" model="gpt-5-2-2025-12-11" apiKey="$var{OPENAI_API_KEY}" baseUrl="https://codemie.lab.epam.com/code-assistant-api/v1">
  <Prompt name="responseText">Write a short poem about the beauty of nature.</Prompt>
  <Out name="responseText" level="info" />
</Extern>
```

CodeMie processor example (based on `src/ae/recipes/codemie/CodeMie Test.recipe`):

```xml
<Var name="CodeMie Username" init="mandatory" />
<Var name="CodeMie Password" init="mandatory" type="password" />
<Var name="User Prompt" init="mandatory" type="text" />

<Extern class="CodeMie" username="$var{CodeMie Username}" password="$var{CodeMie Password}" model="gpt-4o-2024-11-20">
  <Prompt name="CodeMie Response">
    <message role="system">You are an assistant. If web page content is provided, review it and answer accordingly.</message>
    <message role="user">User request: $var{User Prompt}</message>
  </Prompt>
</Extern>

<View name="codeMieResponseView" reuse="false" type="Markdown" />
<Out view="codeMieResponseView">$var{CodeMie Response}</Out>
```
