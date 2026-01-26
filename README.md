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
    - Analyze class foles in `/src/main/java/com/ganteater/ae/desktop/editor` folder.
    - Describe their functionality.
    - Reference `src/ae/ae.xml` as a usage example.
  - **Processors:**  
    - Analyze class files in `/src/main/java/com/ganteater/ae/processor` folder.
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
This project adds AI assistance to Anteater in two ways:

- **Desktop editor helper:** A lightweight, always-available dialog inside the desktop editor that captures your current recipe content (including caret/selection), enriches it with runtime context (available commands, variables, and UI/view descriptions), sends it to an OpenAI-compatible service, and applies the returned update back into the editor.
- **AI-enabled processors for recipes:** Recipe components that let you prompt a model, register tasks as callable tools (functions), list available models, and optionally enable provider features such as web search.

## Installation Instructions
### Prerequisites
- **Java:** 9 (or later)
- **Build tool:** Maven
- **Anteater:** Desktop or CLI installation (for running the plugin)

### Option 1: Clone and Build
1. Clone the repository:
   ```bash
   git clone https://github.com/ganteater/ai-ae-plugin.git
   cd ai-ae-plugin
   ```
2. Build with Maven:
   ```bash
   mvn -DskipTests package
   ```
3. Copy the produced JAR into your Anteater `plugins` folder.

### Option 2: Download Assembled Jar
[![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/anteater/files/plugins/ai-ae-plugin.jar/download)

1. Download the JAR using the button above.
2. Place the downloaded `.jar` file into the `plugins` folder of your Anteater installation.

### Option 3: Launch Anteater Desktop or CLI
When Anteater starts, it automatically detects plugins from the `plugins` folder and loads them at startup.

### Option 4: Maven Integration
Add the plugin as a dependency in your project’s `pom.xml`:

```xml
<dependency>
  <groupId>com.ganteater.plugins</groupId>
  <artifactId>ai-ae-plugin</artifactId>
  <version>1.2.10</version>
</dependency>
```

Maven will download the artifact and include it on the classpath during the build.

## Usage
### Code Helpers
The desktop helper provides an in-editor AI dialog that can propose or apply recipe updates.

Key components:
- **AICodeHelper**: Attaches to a text editor instance, reads configuration (model, apiKey, optional baseUrl), creates an OpenAI client, and manages a progress indicator.
- **AIHelperDialog**: Builds the request context (general instructions, output format, system variables, available processors/commands, and view descriptions), sends the request to the model, and applies the returned recipe text plus optional caret/selection updates.
- **CodeMieHelper**: Alternate helper that authenticates via username/password to obtain an access token, then uses the same OpenAI-compatible flow.

Configuration example (helper wiring and credentials) from `src/ae/ae.xml`:

```xml
<Environment xmlns="http://ganteater.com/xml/configuration"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://ganteater.com/xml/configuration https://ganteater.com/xml/configuration/anteater-1.2.4.2.xsd">

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

</Environment>
```

### Processors
These processors are used from Anteater recipes (typically via `<Extern>`) to interact with OpenAI-compatible providers.

Included processors:
- **OpenAI**: Initializes an OpenAI client, sends prompts/messages, registers function tools backed by nested tasks, lists models, and can enable web search.
- **CodeMie**: Obtains an access token using username/password, sets provider base URL, and then delegates to the OpenAI-compatible implementation.
- **AbstractAIProcessor**: A legacy/alternative base abstraction for prompting and tool registration.

Typical workflow:
1. Configure a provider using `<Extern>`.
2. Optionally declare reusable tools with `<Function>` blocks.
3. Call `<Prompt>` to send one or more messages.
4. Store the assistant output in a recipe variable and use it in subsequent steps.

See example recipes under `src/ae/recipes`, including:
- `src/ae/recipes/openai/OpenAI.recipe`
- `src/ae/recipes/openai/Function Tool Test.recipe`
- `src/ae/recipes/openai/WebSearch Tool Test.recipe`
- `src/ae/recipes/codemie/CodeMie Test.recipe`
