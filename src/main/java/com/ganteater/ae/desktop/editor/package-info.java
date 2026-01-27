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
 * Swing-based desktop editor integration for AI-assisted Anteater recipe editing.
 *
 * <p>
 * The classes in this package integrate an AI helper with a {@link com.ganteater.ae.desktop.editor.TextEditor}.
 * They gather context from the running application (for example, system variables, available
 * {@link com.ganteater.ae.processor.Processor} command metadata, and registered
 * {@link com.ganteater.ae.desktop.view.View} descriptions), send that context together with a user prompt to an
 * OpenAI-compatible backend, and then apply the returned edits back into the editor (updated recipe text and caret/
 * selection changes).
 * </p>
 *
 * <h2>Main responsibilities</h2>
 * <ul>
 *   <li>Creating/configuring an OpenAI client from editor configuration (for example {@code apiKey}, {@code baseUrl},
 *       {@code model}).</li>
 *   <li>Displaying a prompt dialog and orchestrating requests (including optional debug logging of context and
 *       responses).</li>
 *   <li>Parsing the model response and updating the editor content and cursor/selection.</li>
 * </ul>
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link com.ganteater.ae.desktop.editor.AICodeHelper} installs the helper UI, reads editor configuration, and
 *       initializes an {@code com.openai.client.OpenAIClient} (typically asynchronously).</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.AIHelperDialog} collects the user prompt, builds the request context,
 *       calls the backend, and applies the returned editor updates.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.CodeMieHelper} is a variant that obtains an access token from CodeMie
 *       credentials before building the OpenAI client.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * // Typically created by the desktop editor for a specific TextEditor instance.
 * AICodeHelper helper = new AICodeHelper(textEditor);
 * helper.showDialog();
 * }
 * </pre>
 */
package com.ganteater.ae.desktop.editor;
