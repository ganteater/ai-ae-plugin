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
 * Desktop editor integration that connects Anteater's recipe editor to an OpenAI-compatible backend.
 *
 * <p>
 * The classes in this package provide an AI helper UI for {@code TextEditor} instances. They collect the current
 * editor state (full recipe text, caret position, and selection range) and assemble additional runtime context,
 * including system variable names, available processors and their command metadata, and view descriptions.
 * The resulting context is sent as a chat-style request using the OpenAI Java client; the model response is
 * expected to be structured text that contains a generated recipe body plus optional caret/selection updates.
 * The generated recipe text is applied back into the editor and the recipe is recompiled/refreshed.
 * </p>
 *
 * <h2>Main responsibilities</h2>
 * <ul>
 *   <li>Initialize an {@code OpenAIClient} using editor configuration (model, API key/base URL, debug).</li>
 *   <li>Gather request context from system variables, processor command metadata, and view descriptions.</li>
 *   <li>Send the request and apply the returned generated recipe code, followed by compile and UI refresh.</li>
 * </ul>
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link com.ganteater.ae.desktop.editor.AICodeHelper} attaches the helper to the editor UI and creates the
 *       OpenAI client and dialog in a background thread.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.AIHelperDialog} builds the request, invokes the model, parses the
 *       response, and updates the editor contents and caret/selection.</li>
 *   <li>{@link com.ganteater.ae.desktop.editor.CodeMieHelper} obtains an API token via {@code CodeMie} and then
 *       creates the OpenAI client using that token.</li>
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
