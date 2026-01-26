/**
 * Anteater recipe processors that integrate with OpenAI-compatible LLM providers.
 *
 * <p>
 * Classes in this package are typically instantiated from an Anteater recipe via {@code <Extern>}.
 * After initialization, a processor exposes recipe commands for:
 * </p>
 * <ul>
 *   <li>sending prompts/messages and storing the assistant output into recipe variables ({@code <Prompt>});</li>
 *   <li>registering recipe {@code <Task>} blocks as callable tools/functions for the model ({@code <Function>});</li>
 *   <li>listing available models ({@code <Models>});</li>
 *   <li>enabling optional provider features (for example, web search) ({@code <WebSearch>}).</li>
 * </ul>
 *
 * <h2>Usage from an Anteater recipe</h2>
 * <p>
 * Provider settings (for example, API key, base URL, and model name) are supplied as attributes on
 * {@code <Extern>}. Anteater recipe variables should be referenced using the {@code $var{...}} syntax.
 * </p>
 *
 * <h3>Configuring a provider</h3>
 * <pre>
 * &lt;Extern class="OpenAI" model="gpt-5-mini" apiKey="$var{OPENAI_API_KEY}" /&gt;
 * </pre>
 *
 * <h3>Prompting</h3>
 * <p>
 * {@code <Prompt>} sends one or more {@code <message>} elements and stores the resulting assistant output into the
 * variable named by the {@code name} attribute.
 * </p>
 * <pre>
 * &lt;Prompt name="answer"&gt;
 *   &lt;message role="user"&gt;Summarize the release notes.&lt;/message&gt;
 * &lt;/Prompt&gt;
 * </pre>
 *
 * <h3>Defining callable tools (functions)</h3>
 * <p>
 * {@code <Function>} registers a tool with the model. Nested {@code <property>} tags define the tool parameters.
 * When the model calls the tool, the processor:
 * </p>
 * <ol>
 *   <li>parses the JSON arguments supplied by the model;</li>
 *   <li>stores each argument value into a recipe variable with the same name as the parameter;</li>
 *   <li>executes the nested recipe {@code <Task>};</li>
 *   <li>returns an optional value read from the recipe variable specified by the {@code return} attribute.</li>
 * </ol>
 * <pre>
 * &lt;Function name="getTicket" description="Fetch a ticket by id" type="object" return="ticket"&gt;
 *   &lt;property name="id" type="string" required="true" /&gt;
 *   &lt;Task&gt;
 *     &lt;!-- recipe code that sets variable "ticket" --&gt;
 *   &lt;/Task&gt;
 * &lt;/Function&gt;
 * </pre>
 *
 * <h2>Notable classes</h2>
 * <ul>
 *   <li>
 *     {@link com.ganteater.ae.processor.OpenAI} integrates with the OpenAI Responses API (or compatible services) and
 *     provides commands such as {@code <Prompt>}, {@code <Function>}, {@code <Models>}, and {@code <WebSearch>}.
 *   </li>
 *   <li>
 *     {@link com.ganteater.ae.processor.CodeMie} obtains an access token using {@code username}/{@code password} and then
 *     delegates OpenAI-compatible calls to {@link com.ganteater.ae.processor.OpenAI}.
 *   </li>
 *   <li>
 *     {@link com.ganteater.ae.processor.AbstractAIProcessor} is a legacy/alternative base that defines prompting and
 *     tool registration in an implementation-agnostic way.
 *   </li>
 * </ul>
 */
package com.ganteater.ae.processor;

/*-
 * @guidance:
 *
 * **IMPORTANT: UPDATE `package-info.java`!**	
 * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 * 		- Use proper Markdown or HTML formatting for readability.
 *      - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 *		- Do not escape `>` in `{@code ... }` blocks.         
 */
