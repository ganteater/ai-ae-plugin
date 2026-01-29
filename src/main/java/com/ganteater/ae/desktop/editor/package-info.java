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
 * This package contains a {@link com.ganteater.ae.desktop.editor.CodeHelper} implementation that integrates a * {@link com.ganteater.ae.desktop.editor.TextEditor} with an OpenAI-compatible backend. It builds request context from:
 * </p>
 * <ul>
 *   <li>bundled markdown resources (general instructions and output format)</li>
 *   <li>runtime metadata (available {@link com.ganteater.ae.processor.Processor} implementations, their commands, and
 *       registered {@link com.ganteater.ae.desktop.view.View} types)</li>
 *   <li>the current editor state (content, caret position, selection)</li>
 * </ul>
 *
 * <p>
 * The backend response is expected to contain updated recipe text and optional caret/selection information, which is
 * then applied to the editor and followed by a recompile/refresh of the recipe panel.
 * </p>
 *
 * <h2>Main entry points</h2>
 * <ul>
 *   <li>{@link com.ganteater.ae.desktop.editor.AICodeHelper} installs the integration on a {@code TextEditor}, reads
 *       model/client configuration from the editor node, and initializes the OpenAI client asynchronously.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.AIHelperDialog} provides the prompt UI and performs the request/response
 *       application to the editor.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.CodeMieHelper} adapts {@code AICodeHelper} to obtain a bearer token via
 *       {@link com.ganteater.ae.processor.CodeMie} and uses it as the client API key.</li>
 * </ul>
 */
package com.ganteater.ae.desktop.editor;
