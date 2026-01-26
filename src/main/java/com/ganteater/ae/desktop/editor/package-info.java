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
 * Desktop editor integration for AI-assisted Anteater recipe editing.
 *
 * <p>
 * Classes in this package integrate a desktop {@code TextEditor} with an OpenAI-compatible chat backend.
 * They assemble editor and runtime context (current recipe content, caret/selection, system variables,
 * available processors/commands, and view descriptions), send it to the model, and apply the returned
 * updated recipe back into the editor.
 * </p>
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link com.ganteater.ae.desktop.editor.AICodeHelper} initializes the helper for a {@code TextEditor},
 *       reads model/debug/credential settings from the editor configuration, and builds an {@code OpenAIClient}.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.AIHelperDialog} collects context, sends the prompt and editor state,
 *       and updates the editor with the model response (including caret/selection changes when provided).</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.CodeMieHelper} variant of {@code AICodeHelper} that retrieves an access
 *       token using CodeMie credentials before creating the OpenAI client.</li>
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
