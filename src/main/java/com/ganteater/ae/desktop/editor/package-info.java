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
 * This package connects a {@link com.ganteater.ae.desktop.editor.TextEditor} to an OpenAI-compatible backend.
 * It loads static prompt context from resources, generates dynamic context (system variables, processor/command
 * metadata, and registered desktop views), submits that context along with the current editor state and the user's
 * prompt, and applies the backend response by updating the recipe text and caret/selection.
 * </p>
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link com.ganteater.ae.desktop.editor.AICodeHelper} installs AI support into an editor, initializes an
 *       {@link com.openai.client.OpenAIClient} asynchronously, and manages progress/cancel wiring.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.AIHelperDialog} displays the prompt UI, builds the request payload, and
 *       applies the resulting JSON update to the editor and triggers recompilation.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.CodeMieHelper} authenticates via
 *       {@link com.ganteater.ae.processor.CodeMie} and uses the retrieved token as the client API key.</li>
 * </ul>
 *
 * <h2>Expected backend output</h2>
 * <p>
 * The backend is expected to return a JSON object containing {@code generatedOutputRecipeCode} and optionally
 * {@code caretPosition} and {@code selection} (with {@code startPosition} and {@code endPosition}).
 * </p>
 */
package com.ganteater.ae.desktop.editor;
