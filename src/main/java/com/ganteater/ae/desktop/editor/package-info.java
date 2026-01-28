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
 * Desktop editor integration for AI-assisted editing of Anteater recipe text.
 *
 * <p>
 * The types in this package attach an AI helper UI to a {@link com.ganteater.ae.desktop.editor.TextEditor}. They gather
 * the current editor state (content, caret, selection) along with runtime context (available processors/commands, views,
 * and system variables), send that combined input to an OpenAI-compatible backend, and apply the returned JSON result
 * back into the editor.
 * </p>
 *
 * <h2>Main components</h2>
 * <ul>
 *   <li>{@link com.ganteater.ae.desktop.editor.AICodeHelper} installs the integration, initializes an
 *       {@link com.openai.client.OpenAIClient} asynchronously based on editor-node attributes (such as {@code apiKey},
 *       {@code baseUrl}, {@code model}, {@code debug}), and exposes a progress indicator.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.AIHelperDialog} provides the prompt UI, loads static markdown context
 *       resources, serializes editor/context models to JSON, performs the request, and applies the response by updating
 *       text and optionally caret/selection.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.CodeMieHelper} specializes {@link com.ganteater.ae.desktop.editor.AICodeHelper}
 *       to obtain an access token via {@link com.ganteater.ae.processor.CodeMie} credentials and use it when creating
 *       the OpenAI client.</li>
 * </ul>
 *
 * <h2>Response contract</h2>
 * <p>
 * The backend response is expected to be JSON containing {@code generatedOutputRecipeCode} and optionally
 * {@code caretPosition} and {@code selection} (with {@code startPosition} and {@code endPosition}). After applying the
 * update, the editor recompiles the recipe and refreshes related UI state.
 * </p>
 */
package com.ganteater.ae.desktop.editor;
