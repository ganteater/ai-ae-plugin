# IA Anteater Plugin

![](src/site/resources/images/ai-ae-plugin.png)

The **AI-AE-Plugin** is an Anteater plugin that introduces support for AI-powered workflows by integrating with Large Language Models (LLMs) like OpenAI GPT. This plugin enables users to generate content dynamically within Anteater recipes and provides command processors.

**Features**:

- **AI-Powered Recipe Code Generation**:
		Use the `<Editor helper="AIHelper" apiKey="$var{OPENAI_API_KEY}"/>` configuration to interact with LLMs and generate recipe code.
- **AI-Powered Content Generation**:
		Use the `<Prompt>` command to interact with LLMs and generate text-based responses.
- **Customizable Models**:
		Specify the model (e.g., `gpt-4`) to tailor the response to your needs.
- **Seamless Integration**:
		Easily integrate AI responses into your Anteater workflows using variables and output commands.

## Prerequisites

1. **OpenAI API Key**:
	- Ensure you have a valid OpenAI API key. Store the key in a variable (e.g., `$var{OPENAI_API_KEY}`) for secure access.
2. **Anteater with AI-AE-Plugin**:
	- Download and install the AI-AE-Plugin as part of your Anteater setup.

## Installation

1. Maven coordinates: [![Maven Central](https://img.shields.io/maven-central/v/com.ganteater.plugins/ai-ae-plugin.svg)](https://search.maven.org/artifact/com.ganteater.plugins/ai-ae-plugin)
1. Download the Plugin:
	- Obtain the [AI-AE-Plugin JAR file](https://sourceforge.net/projects/anteater/files/plugins/ai-ae-plugin.jar/download) and place it in the `plugins` directory of your Anteater setup.
3. Run Anteater:
	- Execute your recipe using Anteater as usual. The plugin will handle all interactions with the LLM.

## AI Code Helper

### Configuration

To use the **AI Code Helper** feature, you need to add the `<Editor>` configuration tag in your Anteater configuration file (`ae.xml`). This enables the integration with AI-powered tools like OpenAI for code assistance.

#### Example Configuration
Below is an example of how to configure the AI Code Helper in `ae.xml`:

```xml
<Environment>
	<Configuration name="My Configuration">
		<!-- Define the OpenAI API Key -->
		<Var name="OPENAI_API_KEY" init="console" type="password" />

		<!-- Enable AI Code Helper -->
		<Editor helper="AIHelper" apiKey="$var{OPENAI_API_KEY}"/>
	</Configuration>
</Environment>
```

**`<Editor>`**:

- The `helper="AIHelper"` attribute specifies the AI Code Helper to enable.
- The `apiKey` attribute specifies the apiKey to authenticate OpenAI requests.
- The `debug` enables debug mode for logging additional information during LLM request execution (optional).

With this configuration, Anteater is ready to leverage AI capabilities for code assistance, enhancing your workflows with intelligent suggestions and automation.

## LLM Command Processor

### Command: `<Prompt>`
The `<Prompt>` command is used to send a query to an LLM and store the generated response in a variable. This command is highly flexible and can be used in a variety of scenarios, such as content generation, summarization, or creative tasks.

#### Attributes
- **`name`**:
		Defines the variable name where the LLM's response will be stored.
- **`model`**:
		Specifies the LLM model to use (e.g., `gpt-4`, `gpt-3.5-turbo`).
- **Text Content**:
		The content inside the `<Prompt>` tag is the query or instruction sent to the LLM.

### Example Usage

Below is an example recipe that demonstrates how to use the `<Prompt>` command with the AI-AE-Plugin to generate a short poem about the beauty of nature.

```xml
<Recipe name="LLM">
    <About>
        <description>
            This recipe demonstrates the use of the LLM processor to generate a short poem about the beauty of nature using the GPT-4 model. It requires an OpenAI API key for access.
        </description> 
    </About> 
    <Extern class="LLM" apiKey="$var{OPENAI_API_KEY}">
        <Prompt name="responseText" model="gpt-4">Write a short poem about the beauty of nature.</Prompt> 
        <Out name="responseText" level="info" /> 
    </Extern> 
</Recipe>
```

#### Explanation
1. `<Extern>`:
	- The `class` attribute specifies the processor to use (`LLM` in this case).
	- The `apiKey` attribute provides the OpenAI API key, which is required to access the GPT model. The API key can be stored as a variable (e.g., `$var{OPENAI_API_KEY}`).

2. `<Prompt>`:
	- Sends the query `Write a short poem about the beauty of nature` to the GPT-4 model.
	- The response is stored in the variable `responseText`.

3. `<Out>`:
	- Outputs the response stored in `responseText` to the console at the `info` log level.

### Additional Notes

- Supported Models:
	- The plugin currently supports OpenAI models such as `gpt-4` and `gpt-3.5-turbo`.
- Error Handling:
	- Ensure the API key is valid and has sufficient permissions to access the specified model.
- Extensibility:
	- The plugin can be extended to support additional LLM providers or custom APIs.

## Resources

- Anteater Documentation: [http://ganteater.com](http://ganteater.com)
- OpenAI API Documentation: [https://platform.openai.com/docs/](https://platform.openai.com/docs/)


