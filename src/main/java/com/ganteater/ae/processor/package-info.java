/**
 * Anteater recipe processors that integrate with OpenAI-compatible LLM providers.
 *
 * <p>
 * This package provides processors that are instantiated from an Anteater recipe via {@code <Extern>} and then expose
 * recipe commands for interacting with a model:
 * </p>
 * <ul>
 *   <li>{@code <Prompt>} sends one or more {@code <message>} elements and stores the assistant output into a recipe
 *   variable;</li>
 *   <li>{@code <Function>} registers a recipe {@code <Task>} block as a callable tool/function that the model can invoke
 *   with JSON arguments;</li>
 *   <li>{@code <Models>} lists the available model ids from the provider;</li>
 *   <li>{@code <WebSearch>} enables optional provider features (for example, web search) for subsequent prompts.</li>
 * </ul>
 *
 * <p>
 * Provider settings (for example, API key, base URL, and default model name) are supplied as attributes on
 * {@code <Extern>}. Anteater recipe variables should be referenced using the {@code $var{...}} syntax.
 * </p>
 *
 * <h2>Usage from an Anteater recipe</h2>
 *
 * <h3>Configuring a provider</h3>
 * <pre>
 * &lt;Extern class="OpenAI" model="gpt-5-mini" apiKey="$var{OPENAI_API_KEY}" /&gt;
 * </pre>
 *
 * <h3>Prompting</h3>
 * <pre>
 * &lt;Prompt name="answer"&gt;
 *   &lt;message role="user"&gt;Summarize the release notes.&lt;/message&gt;
 * &lt;/Prompt&gt;
 * </pre>
 *
 * <h3>Defining callable tools (functions)</h3>
 * <p>
 * {@code <Function>} registers a tool with the model. Nested {@code <property>} tags define the tool parameters.
 * When the model calls the tool, the processor parses the JSON arguments, stores each argument value into a recipe
 * variable with the same name as the parameter, executes the nested {@code <Task>}, and optionally returns a value read
 * from the recipe variable named by the {@code return} attribute.
 * </p>
 * <pre>
 * &lt;Function name="getTicket" description="Fetch a ticket by id" type="object" return="ticket"&gt;
 *   &lt;property name="id" type="string" required="true" /&gt;
 *   &lt;Task&gt;
 *     &lt;!-- recipe code that sets variable &quot;ticket&quot; --&gt;
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
 *     {@link com.ganteater.ae.processor.CodeMie} obtains an access token using a username/password pair and then delegates
 *     OpenAI-compatible calls to {@link com.ganteater.ae.processor.OpenAI}.
 *   </li>
 *   <li>
 *     {@link com.ganteater.ae.processor.AbstractAIProcessor} is a legacy/alternative base class that defines prompting and
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
