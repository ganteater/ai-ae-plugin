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

/**
 * Desktop editor integration for AI-assisted editing of Anteater recipes.
 *
 * <p>
 * Types in this package connect the Anteater Desktop {@code TextEditor} to an OpenAI-compatible backend. The
 * integration gathers the active editor state (full document text, caret position, and selection range) and enriches
 * it with runtime context such as system variable names, available command/processors, and view descriptions. This
 * information is sent as a chat-style request; the model response is then interpreted as structured output and applied
 * back into the editor as a new recipe body along with optional caret/selection updates.
 * </p>
 *
 * <h2>Main responsibilities</h2>
 * <ul>
 *   <li>Create and configure an {@code OpenAIClient} using editor configuration (model, API key/base URL, debug).</li>
 *   <li>Build context inputs from system variables, {@code Processor} command metadata, and {@code View} descriptions.</li>
 *   <li>Send a request and apply the returned generated recipe text, followed by compile/refresh of the editor view.</li>
 * </ul>
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link com.ganteater.ae.desktop.editor.AICodeHelper} wires the helper into the editor UI and initializes the
 *       dialog and OpenAI client.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.AIHelperDialog} assembles inputs, issues the request, and applies the
 *       model response back into the editor.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.CodeMieHelper} extends {@link com.ganteater.ae.desktop.editor.AICodeHelper}
 *       to obtain an API token via CodeMie before creating the client.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * // Constructed and managed by the desktop editor.
 * AICodeHelper helper = new AICodeHelper(textEditor);
 * helper.showDialog();
 * }
 * </pre>
 */
package com.ganteater.ae.desktop.editor;
