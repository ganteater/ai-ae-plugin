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
 * Desktop editor integration for AI-assisted Anteater recipe editing.
 *
 * <p>
 * This package provides glue code that connects a {@link com.ganteater.ae.desktop.editor.TextEditor} to an
 * OpenAI-compatible backend. It assembles request context (static markdown resources and runtime metadata such as
 * system variables, processors/commands, and registered desktop views), submits the user prompt along with the current
 * editor state, and applies the backend response by updating the editor text and caret/selection.
 * </p>
 *
 * <h2>Main components</h2>
 * <ul>
 *   <li>{@link com.ganteater.ae.desktop.editor.AICodeHelper} installs the helper into an editor, reads model/client
 *       configuration from the editor node, initializes an {@link com.openai.client.OpenAIClient} asynchronously, and
 *       manages progress/cancel wiring.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.AIHelperDialog} displays the prompt UI, builds the request payload, and
 *       applies a JSON response to the editor and triggers recompilation.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.CodeMieHelper} authenticates using
 *       {@link com.ganteater.ae.processor.CodeMie} credentials and uses the retrieved token as the client API key.</li>
 * </ul>
 *
 * <h2>Backend response contract</h2>
 * <p>
 * The backend is expected to return a JSON object containing {@code generatedOutputRecipeCode} and optionally
 * {@code caretPosition} and {@code selection} (with {@code startPosition} and {@code endPosition}).
 * </p>
 */
package com.ganteater.ae.desktop.editor;
