<!--
@guidance:
Content Structure:
# Project Title
- Extract the project title from pom.xml and display it as the main heading.
- Insert `![](src/site/resources/images/ai-ae-plugin.png)` above the title.
- Directly below the title, add a Maven Central badge as a new paragraph:  
  `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])`  
  Replace `[groupId]` and `[artifactId]` with values from pom.xml.
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
    - Explain that Maven will handle downloading and including the plugin during the build.
# Usage
- Provide instructions for:
  - **Code Helpers:**  
    - Analyze classes in `/src/main/java/com/ganteater/ae/desktop/editor`.
    - Describe their functionality.
    - Reference `src/ae/ae.xml` as a usage example.
  - **Processors:**  
    - Analyze classes in `/src/main/java/com/ganteater/ae/processor`.
    - Describe their functionality.
    - Outline a typical workflow for using these components.
    - Reference example recipes in `src/ae/recipes`.
**Formatting Requirements:**
- Use Markdown syntax for all headings, lists, code blocks, and links.
- Ensure each section is clear, concise, and logically organized.
-->

![](src/site/resources/images/ai-ae-plugin.png)

# AI Anteater Plugin

[![Maven Central](https://img.shields.io/maven-central/v/com.ganteater.plugins/ai-ae-plugin.svg)](https://central.sonatype.com/artifact/com.ganteater.plugins/ai-ae-plugin)

## Overview
This plugin adds AI-assisted capabilities to Anteater, enabling automated workflows and interactive assistance powered by OpenAI-compatible language models.

It provides:
- A desktop-editor helper that can send the current recipe text (including caret/selection) plus runtime context to an AI backend and apply the returned updates back into the editor.
- Recipe processors that expose commands for prompting, defining callable tools/functions, listing models, and enabling optional provider features like web search.

## Installation Instructions
### Prerequisites
- Java 9 (as configured by the project)
- Maven (to build or consume the artifact)
- An Anteater installation (Desktop or CLI) to load the plugin
- An API key (or provider credentials) for an OpenAI-compatible service

### Clone and Build
1. Clone the repository:
   ```bash
   git clone https://github.com/ganteater/ai-ae-plugin.git
   cd ai-ae-plugin
   ```
2. Build with Maven:
   ```bash
   mvn -DskipTests package
   ```
3. Copy the produced jar into your Anteater `plugins` folder.

### Download Assembled Jar
[![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/anteater/files/plugins/ai-ae-plugin.jar/download)

1. Download the jar using the link above.
2. Place the downloaded `.jar` file into the `plugins` folder of your Anteater installation.

### Launch Anteater Desktop or CLI
Start Anteater as usual. It will automatically detect and load the plugin from the `plugins` folder.

### Maven Integration
Add the dependency to your project’s `pom.xml`:
```xml
<dependency>
  <groupId>com.ganteater.plugins</groupId>
  <artifactId>ai-ae-plugin</artifactId>
  <version><!-- use the latest version from Maven Central --></version>
</dependency>
```
Maven will download the plugin and include it on the classpath during your build.

## Usage
### Code Helpers
The desktop editor integration provides a lightweight UI for AI-assisted recipe editing:
- **AICodeHelper** initializes an OpenAI-compatible client from editor configuration, collects runtime context, and shows a helper dialog while reporting progress.
- **AIHelperDialog** builds a request that includes system variables, available processors/commands, view information, and the editor’s current content/caret/selection; it sends the request and applies the returned recipe/caret/selection updates back into the editor.
- **CodeMieHelper** is a variant that obtains an access token using CodeMie credentials and then creates the OpenAI-compatible client.

Configuration example (see `src/ae/ae.xml`):
```xml
<Editor helper="AICodeHelper" apiKey="$var{OPENAI_API_KEY}" />

<Editor helper="CodeMieHelper"
  username="$var{CodeMie Username}" password="$var{CodeMie Password}"
  model="gpt-5-2-2025-12-11" />
```

### Processors
The processors are designed to be used from recipes (typically via `Extern`) and provide commands for calling OpenAI-compatible services.

Included components:
- **OpenAI**: connects to the OpenAI Responses API (or compatible services) and provides commands such as `Prompt`, `Function`, `Models`, and `WebSearch`.
- **CodeMie**: obtains an access token using `username`/`password`, configures the underlying OpenAI-compatible client, then reuses the OpenAI processor behavior.
- **AbstractAIProcessor**: a legacy/alternative base that defines prompting and tool registration in a provider-agnostic manner.

Typical workflow:
1. Configure a provider via `Extern` (API key, optional base URL, default model).
2. Optionally register callable tools via `Function` (nested `property` and `Task`).
3. Send prompts via `Prompt` and store the model output in a recipe variable.
4. Optionally list models via `Models` or enable `WebSearch`.

Example recipe usage (see examples under `src/ae/recipes`):
```xml
<Extern class="OpenAI" model="gpt-5-mini" apiKey="$var{OPENAI_API_KEY}" baseUrl="https://example.com/openai">
  <Prompt name="responseText">Write a short poem about the beauty of nature.</Prompt>
  <Out name="responseText" level="info" />
</Extern>

<Function name="getTicket" description="Fetch a ticket by id" type="object" return="ticket">
  <property name="id" type="string" required="true" />
  <Task>
    <!-- recipe code that sets variable "ticket" -->
  </Task>
</Function>
```
