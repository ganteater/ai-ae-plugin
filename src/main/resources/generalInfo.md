# About Anteater Application

Anteater is an **interactive command runner** that uses **recipe XML files** to describe tasks and workflows. It acts as an **External Accessor**, serving as an interface (or "head") for performing actions on testable or controllable applications or systems.

## About Anteater Recipes

The Anteater recipe is an XML file that calls processor command methods. It doesn’t use an XSD schema, allowing flexible combinations of attributes and methods, as defined by the command methods. The XML must be formally valid and include a root <Recipe> tag with a required name attribute.

## Prompt Hints

1. **Recipe Structure**:
	- Ensure the recipe code is always enclosed within the `<Recipe name="[RECIPE_NAME]">` tag.

2. **Command Processor**:
	- Do you know required command processor?
	- If a command processor is requested but no information is provided about it, call the `getProcessorHelp` function tool to obtain details.
	- Do not modify the processor name (e.g., adding suffixes or packages).
	- If the processor description indicates that a class is not found, suggest checking the availability of the required plugin and recommend visiting the documentation page: [https://ganteater.com/ae-plugins/index.html](https://ganteater.com/ae-plugins/index.html).

3. **Selected Text Marking**:
	- If the output code contains the characters `...`, mark them as selected text using `[SELECTION_START]...[SELECTION_END]`.

4. **Escaping Special Characters**:
	- Tag attribute values must not contain `<` or `>` characters. These must be escaped as `&lt;` and `&gt;`.
	- The text content of recipe XML elements must also escape `<` and `>` to `&lt;` and `&gt;`, even within code sections marked by backquotes. For example:
	  - `<About>` ? `&lt;About&gt;`

5. **Extern Tag**:
	- If the recipe code contains commands that are not specific to the core processor, ensure they are enclosed within the `Extern` tag.
	- If the `Extern` tag is missing, create it.

6. **Comments for Messages**:
	- Use XML comments to display messages, e.g., `<!-- the message -->`.

7. **Output Requirements**:
	- The response should contain recipe XML code only.
	- If a message needs to be displayed, use an XML comment within the recipe code.

8. **Attribute Value Replacement**:
	- Replace the `type:` prefix in attributes with an appropriate value that matches its intended purpose.
	- Example: `name="type:property"` ? Replace `type:` with the correct value.

9. **Variable Access**:
	- Variables are directly accessible in the template using the syntax: `$var{VARIABLE_NAME}`.