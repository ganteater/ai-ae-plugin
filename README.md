<!--
@guidance:
Content Structure:
# Project Title
- Extract the project title from pom.xml and display it as the main heading.
- Immediately after the title, add a Maven Central badge as a new paragraph:  
  `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])`  
  Replace `[groupId]` and `[artifactId]` with values from pom.xml.
# Overview
- Summarize the content of all `package-info.java` files found in the source folders.
- Describe the application's core functionality in a professional, non-technical style. Do not mention package names.
- Provide a concise summary outlining the project's purpose and main features.
# Installation Instructions
- List all prerequisites, including the required Java version and build tools.
- Describe multiple ways to use this plugin:
  - **Clone and Build the Project:**  
    - Provide clear, step-by-step instructions for checking out the repository and building the project with Maven.
  - **Download Assembled Jar File:**  
    - Include a direct download link for the application jar:  
      `[![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/anteater/files/plugins/ai-ae-plugin.jar/download)`
    - Instruct the user to open the folder where Anteater is installed, locate the `plugins` folder, and move or copy the downloaded `.jar` file into it.
  - **Launch Anteater Desktop or CLI:**  
    - Explain that the application will automatically detect and load the plugin, making its features available.
  - **Use Maven to Manage Your Project:**  
    - Show how to add the plugin as a dependency in the project’s `pom.xml` file.
    - Explain that Maven will download and include the plugin automatically during the build process.
# Usage
- Provide usage instructions for:
  - **Code Helpers:**  
    - Describe how to use code helpers defined in `/src/main/java/com/ganteater/ae/desktop/editor`.
  - **Processors:**  
    - Describe how to use processors in `/src/main/java/com/ganteater/ae/processor`.
    - Outline a typical workflow for using the project artifacts.
    - Reference example recipes found in `src/ae/recipes`.
**Formatting Requirements:**
- Use Markdown syntax for all headings, lists, code blocks, and links.
- Ensure each section is clear, concise, and logically organized.
-->

# AI Anteater Plugin

[![Maven Central](https://img.shields.io/maven-central/v/com.ganteater.plugins/ai-ae-plugin.svg)](https://central.sonatype.com/artifact/com.ganteater.plugins/ai-ae-plugin)

## Overview

This plugin adds AI-assisted capabilities to Anteater in two complementary areas:

- **Desktop editor assistance**: integrates with the Anteater Desktop editor to send the current document (including selection and caret position) together with relevant runtime context, then applies the AI-generated recipe text back into the editor and refreshes the view.
- **AI-enabled recipe processors**: provides processors that connect to OpenAI-compatible providers, enabling recipes to send prompts, register recipe tasks as callable tools (functions), list available models, and optionally enable provider features such as web search.

In practical terms, it helps you draft, refine, and automate recipe-based workflows by combining your existing recipe context with an AI model that can generate or transform recipe content.

## Installation Instructions

### Prerequisites

- **Java**: 9 (or compatible toolchain configured for Java 9)
- **Build tool**: Maven
- **Anteater**: Anteater Desktop and/or CLI installed (to load and run plugins)

### Clone and build the project

1. Clone the repository:

   ```bash
   git clone https://github.com/ganteater/ai-ae-plugin.git
   cd ai-ae-plugin
   ```

2. Build with Maven:

   ```bash
   mvn -DskipTests package
   ```

3. (Optional) If your build produces an assembled plugin jar via a packaging profile, build it as well:

   ```bash
   mvn -DskipTests package -Ppack
   ```

### Download assembled jar file

[![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/anteater/files/plugins/ai-ae-plugin.jar/download)

1. Download the jar using the link above.
2. Open the folder where Anteater is installed.
3. Locate the `plugins` folder.
4. Move or copy the downloaded `ai-ae-plugin.jar` into the `plugins` folder.

### Launch Anteater Desktop or CLI

Start Anteater Desktop or the Anteater CLI. Anteater will automatically detect and load the plugin from the `plugins` folder, making the AI features available.

### Use Maven to manage your project

Add the plugin as a dependency in your project’s `pom.xml`:

```xml
<dependency>
  <groupId>com.ganteater.plugins</groupId>
  <artifactId>ai-ae-plugin</artifactId>
  <version><!-- use the latest version from Maven Central --></version>
</dependency>
```

Maven will download and include the dependency automatically during your build.

## Usage

### Code helpers

The code helper integration is intended for the Anteater Desktop editor.

General workflow:

1. Open a recipe in Anteater Desktop.
2. Invoke the AI code helper from the editor UI.
3. Provide or confirm provider settings (model, API key and/or base URL, debug options).
4. The helper collects editor state (text, selection/caret) plus relevant context (available processors/commands, system variables, view information).
5. The AI response is applied back into the editor as updated recipe text, and the editor view is refreshed.

### Processors

These processors are used from Anteater recipes (XML) and connect to OpenAI-compatible backends.

Typical workflow:

1. Configure a provider in a recipe using an `Extern` element.
2. Send prompts/messages and store the output to a variable.
3. Optionally register recipe `Task` blocks as callable tools (functions).
4. Use the returned variable values in subsequent recipe steps.

Example snippets (see also `src/ae/recipes`):

Configure a provider:

```xml
<Extern class="OpenAI" model="gpt-5-mini" apiKey="$var{OPENAI_API_KEY}" />
```

Prompting:

```xml
<Prompt name="answer">
  <message role="user">Summarize the release notes.</message>
</Prompt>
```

Define a callable function tool:

```xml
<Function name="getTicket" description="Fetch a ticket by id" type="object" return="ticket">
  <property name="id" type="string" required="true" />
  <Task>
    <!-- recipe code that sets variable "ticket" -->
  </Task>
</Function>
```
