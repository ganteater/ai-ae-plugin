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
 * Abstract base processor for LLM-backed Anteater integrations.
 *
 * <p>
 * This class centralizes shared behavior used by concrete provider processors. It supports initialization from an
 * Anteater {@code <Extern>} element and provides common recipe commands for sending prompts and registering callable
 * tools/functions.
 * </p>
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

	@CommandDescription("The \"name\" attribute specifies the variable where the response will be stored.")
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

	@CommandDescription(
			"Registers a callable tool/function. Nested <property> tags describe parameters; the nested <Task> is executed when the model requests the function.")
	@CommandExamples({
			"<Function name=\"type:string\" description=\"type:string\" type=\"enum:object|number|boolean|array|null|string\" return=\"type:property\">"
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
