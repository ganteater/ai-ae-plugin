package com.ganteater.ae.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ganteater.ae.CommandException;
import com.ganteater.ae.processor.annotation.CommandDescription;
import com.ganteater.ae.processor.annotation.CommandExamples;
import com.ganteater.ae.util.xml.easyparser.Node;

/**
 * Base processor for implementing LLM-backed Anteater commands.
 *
 * <p>
 * This class provides common behavior for processors that:
 * </p>
 * <ul>
 *   <li>are configured via {@code <Extern>} attributes such as {@code model} and {@code apiKey}</li>
 *   <li>expose {@code <Prompt>} to send one or more {@code <message>} elements and store the response into a variable</li>
 *   <li>expose {@code <Function>} to register a recipe {@code <Task>} block as a callable tool/function</li>
 * </ul>
 *
 * <p>
 * Concrete implementations must provide:
 * </p>
 * <ul>
 *   <li>{@link #perform(List)} to send prompt inputs and return a text response</li>
 *   <li>{@link #addFunctionTool(Node, Map)} to register a tool definition with the provider implementation</li>
 * </ul>
 */
public abstract class AbstractAIProcessor extends BaseProcessor {

	static final String DEFAULT_ROLE = "user";
	static final String DEFAULT_MODEL_NAME = "gpt-5-mini";

	private String chatModel;
	private String apiKey;

	public void init(Processor parentProcessor, Node action) throws CommandException {
		super.init(parentProcessor, action);
		chatModel = attr(action, "model", DEFAULT_MODEL_NAME);

		apiKey = attr(action, "apiKey");
		if (StringUtils.isBlank(getApiKey())) {
			throw new CommandException("apiKey is required.", parentProcessor);
		}
	}

	@CommandDescription("The 'name' attribute is used to define the property name where the response will be stored.")
	@CommandExamples({ "<Prompt name=\"type:property\">...</Prompt>",
			"<Prompt name=\"type:property\"><message>...</message></Prompt>" })
	public void runCommandPrompt(Node action) throws CommandException {
		String name = action.getAttribute("name");
		ArrayList<String> inputs = new ArrayList<>();

		for (Node node : action) {
			switch (node.getTag()) {
			case "message":
				inputs.add(node.getInnerText());
				break;

			case "$Text":
				inputs.add(action.getInnerText());
				break;

			default:
				break;
			}
		}

		String value = perform(inputs);
		setVariableValue(name, value);
	}

	@CommandDescription("Function command to create a function tool. Property tags define the properties of the function tool. "
			+ "The Task command is called when the model requests the function.")
	@CommandExamples({
			"<Function name=\"type:string\" description=\"type:string\" type=\"enum:object|number|boolean|array|null|string\" return=\"type:proprty\">"
					+ "<property name=\"type:string\" type=\"type:string\" required=\"type:boolean\"/>"
					+ "<Task>...recipe code...</Task>" + "</Function>" })
	public void runCommandFunction(Node action) {

		Node[] props = action.getNodes("property");

		Map<String, Map<String, String>> propsMap = new HashMap<>();
		for (Node propNode : props) {
			String paramName = propNode.getAttribute("name");
			String paramType = propNode.getAttribute("type");
			String required = attr(propNode, "required", "false");

			Map<String, String> value = new HashMap<>();
			value.put("type", paramType);
			value.put("required", required);
			propsMap.put(paramName, value);
		}

		addFunctionTool(action, propsMap);
	}

	public String getChatModel() {
		return chatModel;
	}

	public String getApiKey() {
		return apiKey;
	}

	protected abstract void addFunctionTool(Node action, Map<String, Map<String, String>> propsMap);

	protected abstract String perform(List<String> inputs) throws CommandException;

}