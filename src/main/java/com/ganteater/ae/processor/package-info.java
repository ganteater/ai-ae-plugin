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
 * This package contains {@link com.ganteater.ae.processor.BaseProcessor} implementations intended to be instantiated from
 * an Anteater recipe via {@code <Extern>}. They provide recipe commands for sending prompts, registering callable
 * tools/functions backed by recipe {@code <Task>} blocks, and querying provider metadata.
 * </p>
 *
 * <h2>Included processors</h2>
 * <ul>
 *   <li>{@link com.ganteater.ae.processor.OpenAI}: OpenAI Responses API client (and compatible services).</li>
 *   <li>{@link com.ganteater.ae.processor.CodeMie}: authenticates to CodeMie, then delegates to {@link com.ganteater.ae.processor.OpenAI}
 *       using a CodeMie base URL.</li>
 *   <li>{@link com.ganteater.ae.processor.AbstractAIProcessor}: legacy/alternate base class for provider implementations.</li>
 * </ul>
 *
 * <h2>Recipe integration</h2>
 * <p>
 * Provider settings such as API key, base URL, and default model are supplied as attributes on {@code <Extern>}. Anteater
 * recipe variables are referenced using {@code $var{...}}.
 * </p>
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
 * <pre>
 * &amp;lt;Function name="getTicket" description="Fetch a ticket by id" type="object" return="ticket"&amp;gt;
 *   &amp;lt;property name="id" type="string" required="true" /&amp;gt;
 *   &amp;lt;Task&amp;gt;
 *     &amp;lt;!-- recipe code that sets variable &quot;ticket&quot; --&amp;gt;
 *   &amp;lt;/Task&amp;gt;
 * &amp;lt;/Function&amp;gt;
 * </pre>
 */
package com.ganteater.ae.processor;
