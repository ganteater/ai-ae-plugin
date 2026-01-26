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
 * This package connects the desktop {@code TextEditor} to an OpenAI-compatible backend. It gathers the current editor
 * state (document text, caret position, selection), combines it with runtime application context (system variables,
 * command processor metadata, and view descriptions), and submits that information as a chat-style request. The model
 * response is then applied back to the editor, including text edits and caret/selection updates, followed by any
 * necessary refresh/recompile steps.
 * </p>
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link com.ganteater.ae.desktop.editor.AICodeHelper} wires the editor UI integration, reads configuration from the
 *       editor configuration node (for example: model, API/base URL, debug), creates an
 *       {@link com.openai.client.OpenAIClient}, and provides helpers for assembling runtime context.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.AIHelperDialog} loads prompt resources, serializes runtime context, sends a
 *       request to the model, interprets the returned JSON, and applies edits back into the editor.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.CodeMieHelper} is an {@link com.ganteater.ae.desktop.editor.AICodeHelper}
 *       variant that obtains an API token via CodeMie credentials before initializing the OpenAI client.</li>
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
