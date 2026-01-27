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
 * This package wires a desktop {@code TextEditor} to an OpenAI-compatible chat backend. It builds a
 * context payload (static prompt resources, available processors and their commands, system variables,
 * and view descriptions), submits the user's prompt plus the current editor state, then applies the
 * model response back into the editor.
 * </p>
 *
 * <h2>How it works</h2>
 * <ol>
 *   <li>{@link com.ganteater.ae.desktop.editor.AICodeHelper} attaches an AI helper UI element to the editor,
 *       reads configuration from the editor node (for example {@code model}, {@code debug}, {@code apiKey},
 *       {@code baseUrl}), and asynchronously initializes an {@code OpenAIClient}.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.AIHelperDialog} loads static context resources, enumerates
 *       {@code Processor} and {@code View} classes, serializes the editor state to JSON, submits a request,
 *       and updates the editor with the returned recipe code (optionally adjusting caret/selection).</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.CodeMieHelper} is a specialized {@code AICodeHelper} that obtains
 *       an access token via {@code CodeMie} credentials before creating the OpenAI client.</li>
 * </ol>
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
