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
 * This package contains the UI and orchestration code that connects a desktop {@code TextEditor} with an
 * OpenAI-compatible backend. It captures the editor state (recipe text, caret position, and selection),
 * augments it with runtime context (system variables, available processors/commands, and view descriptions),
 * submits the combined prompt to the backend, and applies the returned update back into the editor.
 * </p>
 *
 * <h2>Workflow</h2>
 * <ol>
 *   <li>{@link com.ganteater.ae.desktop.editor.AICodeHelper} initializes the helper for a specific editor,
 *       builds an {@code OpenAIClient}, and manages progress UI.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.AIHelperDialog} gathers static and dynamic context, issues the
 *       request, and expects a structured response containing the updated recipe (and optionally caret/selection
 *       adjustments).</li>
 *   <li>The dialog updates the editor content, recompiles the current task, refreshes the task tree, and then
 *       applies any returned caret/selection positions.</li>
 * </ol>
 *
 * <h2>Authentication variants</h2>
 * <ul>
 *   <li>{@link com.ganteater.ae.desktop.editor.AICodeHelper} reads an {@code apiKey} (and optional {@code baseUrl})
 *       from the editor configuration.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.CodeMieHelper} obtains a token via {@code CodeMie} using
 *       username/password credentials and then creates the OpenAI client.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * // Typically created by the desktop editor for a specific TextEditor instance.
 * AICodeHelper helper = new AICodeHelper(textEditor);
 * helper.showDialog();
 * }
 * </pre>
 */
package com.ganteater.ae.desktop.editor;
