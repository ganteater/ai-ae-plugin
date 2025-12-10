# About Anteater Application

Anteater is an **interactive command runner** that uses **recipe XML files** to describe tasks and workflows. It acts as an **External Accessor**, serving as an interface (or "head") for performing actions on testable or controllable applications or systems.

## About Anteater Recipes

The Anteater recipe is an XML file that calls processor command methods. It doesn’t use an XSD schema, allowing flexible combinations of attributes and methods, as defined by the command methods. The XML must be formally valid and include a root <Recipe> tag with a required name attribute.

## Prompt Hints

- Ensure the recipe code is always enclosed within the `<Recipe name="[RECIPE_NAME]">` tag.
- Do you know required command processor?
- If a command processor is requested but no information is provided about it, call the `GetProcessorInfo` function tool to obtain details.
- Do not modify the processor name (e.g., adding suffixes or packages).
- If the processor description indicates that a class is not found, suggest checking the availability of the required plugin and recommend visiting the documentation page: [https://ganteater.com/ae-plugins/index.html](https://ganteater.com/ae-plugins/index.html).
- If the output code contains the characters `...`, mark them as selected text using `[SELECTION_START]...[SELECTION_END]`.
- Tag attribute values must not contain `<` or `>` characters. These must be escaped as `&lt;` and `&gt;`.
- Tag attribute values must not contain quotation mark: `"` character. These must be escaped as `&quot;`.
- The text content of recipe XML elements must also be escaped with `<` and `>` characters, using `&lt;` and `&gt;`, even within code fragments enclosed in backticks when composing Markdown formatted text. For example, `<some_text>` should be escaped with `&lt;some_text&gt;`.
- If the recipe code contains commands that are not specific to the core processor, ensure they are enclosed within the `Extern` tag.
- If the `Extern` tag is missing, create it.
- Use XML comments to display messages, e.g., `<!-- the message -->`.
- If a message needs to be displayed, use an XML comment within the recipe code.
- Variables are directly accessible in the template using the syntax: `$var{VARIABLE_NAME}`.
- The variable name is not case-sensitive (except for paths in JSON objects) and can include SPACE characters.
- The variable name must have a human-readable meaning and be displayed in the message dialog box.
- If the command processor class is in the default package `com.ganteater.ae.processor`, you can omit this package name in the class attribute of the <Extern> tag.
- Attribute values for command usage patterns may specify the type of values to use. An attribute's value type begins with the `type:` prefix or `enum`. Here are the rules to apply to generating values based on the specified type:
	- type:property - Refers to a property name within the configuration or environment metadata. Values are interpreted as references to existing properties.
	- type:string - Indicates a textual value. Any sequence of characters is allowed without restrictions.
	- type:path - Specifies a file system path. Values should be a valid file or directory path, either relative or absolute, depending on the context.
	- type:integer - Requires a numeric value that is an integer. Values are parsed as whole numbers without fractional parts.
	- type:regex - Expects a valid regular expression pattern. Values must conform to standard regex syntax for pattern matching.
	- type:boolean - Accepts logical values of `true` or `false`. Case-insensitive variations such as `True` or `FALSE` are also acceptable.
	- type:time - Represents a time, supported suffixes:
		- `Y`: for years, e.g. `timeout="2Y"`;
		- `M`: for month, e.g. `timeout="2M"`;
		- `w`: for weeks, e.g. `timeout="2w"`;
		- `d`: for days, e.g. `timeout="2d"`;
		- `h`: for hours, e.g. `timeout="2h"`;
		- `m`: for minutes, e.g. `timeout="2m"`;
		- `s`: for seconds, e.g. `timeout="2s"`;
		- no suffix: for milliseconds, e.g. `timeout="2"`.
	- type:url - Requires a valid URL format, including the scheme (`http`, `https`) and domain.
	- type:recipe - Refers to a specific Anteater recipe by name or path. Values indicate recipe identifiers for reference.
	- enum: - Specifies a predefined enumeration of values. 
		Only the listed parameters are valid values for the attribute, for example, for `enum:a|b|c` one of the following values should be used: `a`, `b`, `c`.