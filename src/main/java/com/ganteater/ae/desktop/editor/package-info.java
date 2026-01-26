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
 * This package provides a small UI workflow that lets a {@code TextEditor} send the current editor state
 * (recipe text, caret position, and selection) together with runtime metadata (system variables, available
 * processors/commands, and view descriptions) to an OpenAI-compatible backend. The backend response is expected
 * to be structured text containing the updated recipe plus optional caret/selection adjustments, which are then
 * applied back into the editor and followed by a recompile/refresh.
 * </p>
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link com.ganteater.ae.desktop.editor.AICodeHelper} initializes an {@code OpenAIClient}, gathers context,
 *       and manages showing the helper UI while reporting progress.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.AIHelperDialog} collects context and editor state, issues the
 *       request, and applies the generated recipe and caret/selection updates.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.CodeMieHelper} variant that obtains an API token via
 *       {@code CodeMie} and uses it to create the OpenAI client.</li>
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
