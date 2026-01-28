/*-
 * @guidance:
 *
 * - **MANDATORY: UPDATE `package-info.java`.**
 * - **MANDATORY: UPDATE Javadoc for all Java class files in this folder.**
 *
 * - Write Clear and Concise Descriptions:
 *     - Provide meaningful summaries that accurately explain the purpose, behavior, and usage of each class, interface, method, and field.
 *     - Avoid vague or generic statements; be specific about each element’s functionality and intent.
 *
 * - Update `package-info.java`:
 *     - Analyze all source code within this package.
 *     - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
 *     - Do NOT include a "Guidance and Best Practices" section in the `package-info.java` file.
 *     - Place the package-level Javadoc immediately before the `package` declaration.
 *
 * - Include Usage Examples Where Appropriate:
 *     - Add code snippets or usage examples in Javadoc comments for complex classes or methods to illustrate their usage.
 *
 * - Maintain Consistency and Proper Formatting:
 *     - Follow a consistent style and structure for all Javadoc comments.
 *     - Use proper Markdown or HTML formatting to enhance readability.
 *     - For code blocks inside `<pre>` tags in Javadoc, escape `<` as `&lt;` and `>` as `&gt;` within the `<pre>` content.
 *     - Ensure all code in `<pre>` tags is properly escaped and formatted for Javadoc.
 *     - Do NOT escape `>` in `{@code ... }` blocks.
 */

/**
 * Anteater recipe processors that integrate with OpenAI-compatible LLM providers.
 *
 * <p>
 * The classes in this package are intended to be instantiated from an Anteater recipe via {@code <Extern>} and then
 * expose recipe commands for interacting with a model.
 * </p>
 *
 * <h2>Commands</h2>
 * <ul>
 *   <li>{@code <Prompt>} sends one or more {@code <message>} elements and stores the assistant output into a recipe
 *   variable.</li>
 *   <li>{@code <Function>} registers a recipe {@code <Task>} block as a callable tool/function that the model can invoke
 *   with JSON arguments.</li>
 *   <li>{@code <Models>} lists the available model ids from the provider.</li>
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
 * &amp;lt;Extern class="OpenAI" model="gpt-5-mini" apiKey="$var{OPENAI_API_KEY}" /&amp;gt;
 * </pre>
 *
 * <h3>Prompting</h3>
 * <pre>
 * &amp;lt;Prompt name="answer"&amp;gt;
 *   &amp;lt;message role="user"&amp;gt;Summarize the release notes.&amp;lt;/message&amp;gt;
 * &amp;lt;/Prompt&amp;gt;
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
 * &amp;lt;Function name="getTicket" description="Fetch a ticket by id" type="object" return="ticket"&amp;gt;
 *   &amp;lt;property name="id" type="string" required="true" /&amp;gt;
 *   &amp;lt;Task&amp;gt;
 *     &amp;lt;!-- recipe code that sets variable &quot;ticket&quot; --&amp;gt;
 *   &amp;lt;/Task&amp;gt;
 * &amp;lt;/Function&amp;gt;
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
