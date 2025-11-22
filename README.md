![](src/site/resources/images/ai-ae-plugin.png)

# IA Anteater Plugin

The **AI-AE-Plugin** is an Anteater plugin that introduces support for AI-powered workflows by integrating with Large Language Models (LLMs) like OpenAI GPT. This plugin enables users to generate content dynamically within Anteater recipes and provides command processors.

**Features**:

- **AI-Powered Recipe Code Generation**:
		Use the `<Editor helper="AICodeHelper" apiKey="$var{OPENAI_API_KEY}"/>` configuration to interact with LLMs and generate recipe code.
- **AI-Powered Content Generation**:
		Use the commands LLM plugin processors to interact with LLMs and generate text-based responses.
- **Customizable Models**:
		Specify the model (e.g., `gpt-5`) to tailor the response to your needs.
- **Seamless Integration**:
		Easily integrate AI responses into your Anteater workflows using variables and output commands.

## Prerequisites

1. Install Anteater[^1], see: [Run Anteater, Run](https://ganteater.com/run-anteater-run.html).
2. To use the AI-AE-Plugin, you can either download the JAR file directly or add it as a Maven dependency to your project.
	- Download the Plugin: [AI Anteater Plugin](https://sourceforge.net/projects/anteater/files/plugins/ai-ae-plugin.jar/download)
	- Or add as Maven Dependency:  
		You can include the plugin in your Maven project by adding the following dependency to your `pom.xml` file:
		```xml
		<dependency>
			  <groupId>com.ganteater.plugins</groupId>
			  <artifactId>ai-ae-plugin</artifactId>
			  <version>latest_version</version>
		 </dependency>
		 ```
		[![Maven Central](https://img.shields.io/maven-central/v/com.ganteater.plugins/ai-ae-plugin.svg)](https://central.sonatype.com/artifact/com.ganteater.plugins/ai-ae-plugin)
4. Install other plugins (optional), see: [Anteater Plugins](https://ganteater.com/ae-plugins/index.html)
3. OpenAI API Key:
	- Ensure you have a valid OpenAI API key.[^2]

## AI Code Helper

### Configuration

To use the **AI Code Helper** feature, you need to add the `<Editor>` configuration tag in your Anteater configuration file (`ae.xml`). This enables the integration with AI-powered tools like OpenAI for code assistance.

> Note: AICodeHelper only works with the [desktop version of Anteater](https://ganteater.com/anteater-desktop/index.html).

Example Configuration:

Below is an example of how to configure the AI Code Helper in `ae.xml`:

```xml
<Environment>
	<Configuration name="My Configuration">
		<!-- Define the OpenAI API Key -->
		<Var name="OPENAI_API_KEY" init="console" type="password" />

		<!-- Enable AI Code Helper -->
		<Editor helper="AICodeHelper" apiKey="$var{OPENAI_API_KEY}"/>
	</Configuration>
</Environment>
```

Editor attributes:

- The `helper="AICodeHelper"` attribute specifies the AI Code Helper to enable.
- The `apiKey` attribute specifies the apiKey to authenticate OpenAI requests.
- The `model` specifies the OpenAI model to use. Supported values:
  - `gpt-5`
  - `gpt-5-mini` (default)
- The `debug` enables debug mode for logging additional information during LLM request execution (optional).

With this configuration, Anteater is ready to leverage AI capabilities for code assistance, enhancing your workflows with intelligent suggestions and automation.

### How to Use

1. Open the Recipe Editor:  
	Start by opening the recipe editor in your Anteater environment.

2. Activate Code Helper:  
	- Press `Ctrl+Space` or right-click and select **`Code Helper`** from the popup menu.
	- Ensure the cursor is not positioned directly after `<` or on a tag name. Otherwise, the [command helper](https://ganteater.com/anteater-desktop/command-helper.html) will be displayed instead.

3. Input you prompt (e.g.: `create an example of using the Web and the OpenAI processor with their commands`) and click `Perform` button.

	![Code Helper](src/site/resources/images/ai-code-helper.png) 

4. Review the generated [code](src/manual-test/ae/recipes/Web%20And%20OpenAI%20Example.recipe) and try to run.

This feature simplifies the process of writing recipes and ensures accurate syntax and command usage.

## LLM Command Processors

### Command Processor: OpenAI

The **OpenAI processor** is the core component of this plugin, allowing Anteater recipes to call OpenAI services seamlessly. It supports commands for generating responses and managing conversations using OpenAI's models.

Fully Qualified Class Name: `com.ganteater.ae.processor.OpenAI`.

Example:

```xml
<Extern class="OpenAI" apiKey="$var{OPENAI_API_KEY}" model="gpt-5-mini">
	<!-- commands -->
</Extern>
```

#### Supported Commands

##### Command: `<Prompt>`
The `<Prompt>` command is used to send a query to an LLM and store the generated response in a variable. This command is highly flexible and can be used in a variety of scenarios, such as content generation, summarization, or creative tasks.

Attributes:

- **`name`**:
		Defines the variable name where the LLM's response will be stored.
- **Text Content**:
		The content inside the `<Prompt>` tag is the query or instruction sent to the LLM.

Example Usage:

Below is an example recipe that demonstrates how to use the `<Prompt>` command with the AI-AE-Plugin to generate a short poem about the beauty of nature.

```xml
<Prompt name="AI_PROMPT_RESPONSE">
	Please rewrite the poem as a 4-line version using simpler language and an uplifting tone while keeping vivid imagery.
</Prompt>
<Out name="AI_PROMPT_RESPONSE" />
```

```xml
<Prompt name="responseText">
	<message role="system">You are a helpful poet that writes concise, imagery-rich poems.</message>
	<message role="user">Write a short poem about the beauty of nature.</message>
</Prompt>
<Out name="responseText" level="info" />
```

##### Command: `<Function>`

The `<Function>` command is used to create a **Function Tool** in Anteater. This command defines a custom function that can be executed during recipe execution. It allows you to specify inputs, outputs, and the logic for the function, making it a powerful way to encapsulate reusable operations.

Attributes:

- **`name`**:  
  Defines the name of the function. This name is used to call the function during execution.

- **`description`**:  
  A short description of what the function does. This is useful for documenting the purpose of the function.

- **`type`**:  
  Specifies the type of data the function returns (e.g., `object`, `string`, `number`, etc.).

- **`return`**:  
  The variable name where the function's result will be stored after execution.

Child Elements:

- **`<property>`**:  
  Defines the input parameters for the function. Each property includes:
  - **`name`**: The name of the parameter.
  - **`type`**: The data type of the parameter (e.g., `string`, `number`, etc.).
  - **`required`**: Indicates whether this parameter is mandatory (`true` or `false`).

- **`<Task>`**:  
  Specifies the implementation of the function. Within the `<Task>` block, you can define variables, perform operations, and output results.

Example Usage:

Below is an example recipe that demonstrates how to use the `<Function>` command to create a tool that retrieves mock weather information for a given city.

```xml
<Function name="get-weather" description="Get current weather for a city" type="object" return="weatherResult">
	<property name="city" type="string" required="true" />
	<Task>
		<!-- Mock implementation: set weatherResult property -->
		<Var name="weatherResult">{ "city": "$var{city}", "forecast": "sunny" }</Var>
		<Out name="city" level="info" />
		<Out name="weatherResult" level="info" />
	</Task>
</Function>
```

Other Examples:

Here are additional examples of recipes that demonstrate the use of the `<Function>` command and related tools:

- [Create Note](src/manual-test/ae/recipes/Create%20Note.recipe): This recipe showcases how to use the `<Function>` command to create and manage notes dynamically.
- [Function Tool Test](src/manual-test/ae/recipes/Function%20Tool%20Test.recipe): This recipe provides a comprehensive test of the Function Tool, including parameter handling, task execution, and output generation.

These examples can help you better understand how to implement and utilize the `<Function>` command in your own Anteater recipes.

[^1]: Anteater Documentation: [http://ganteater.com](http://ganteater.com)
[^2]: OpenAI API Documentation: [https://platform.openai.com/docs/](https://platform.openai.com/docs/)


