package com.ganteater.ae.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.ganteater.ae.CommandException;
import com.ganteater.ae.processor.annotation.CommandDescription;
import com.ganteater.ae.processor.annotation.CommandExamples;
import com.ganteater.ae.util.xml.easyparser.Node;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonString;
import com.openai.core.JsonValue;
import com.openai.models.responses.FunctionTool;
import com.openai.models.responses.FunctionTool.Parameters;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseCreateParams.Builder;
import com.openai.models.responses.ResponseCreateParams.Input;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseInputItem.Message;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputMessage.Content;
import com.openai.models.responses.Tool;

public class OpenAI extends BaseProcessor {

	private static final String DEFAULT_ROLE = "user";
	private static final String DEFAULT_MODEL_NAME = "gpt-5-mini";
	private OpenAIClient client;
	private String chatModel;

	private Map<Tool, Node> toolsMap = new HashMap<>();

	@Override
	@CommandDescription("OpenAI processor supports command to call OpenAI services.")
	@CommandExamples({
			"<Extern class='OpenAI'  model='enum:gpt-5|gpt-5-mini' apiKey='type:string'" })
	public void init(Processor parentProcessor, Node action) throws CommandException {
		super.init(parentProcessor, action);
		chatModel = attr(action, "model", DEFAULT_MODEL_NAME);

		String apiKey = attr(action, "apiKey");
		if (StringUtils.isBlank(apiKey)) {
			throw new CommandException("apiKey is required.", parentProcessor);
		}

		client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();
	}

	@CommandDescription("The 'name' attribute is used to define the property name where the response will be stored.")
	@CommandExamples({ "<Prompt name='type:property'>...</Prompt>",
			"<Prompt name='type:property'><message role='enum:user|system|developer'>...</message></Messages>" })
	public void runCommandPrompt(Node action) throws CommandException {
		String name = action.getAttribute("name");
		ArrayList<ResponseInputItem> arrayList = new ArrayList<ResponseInputItem>();

		for (Node node : action) {
			switch (node.getTag()) {
			case "message":
				String innerText = node.getInnerText();
				Message message = message(innerText, attr(node, "role", DEFAULT_ROLE));
				arrayList.add(ResponseInputItem.ofMessage(message));
				break;

			case "$Text":
				innerText = action.getInnerText();
				message = message(innerText, attr(node, "role", DEFAULT_ROLE));
				arrayList.add(ResponseInputItem.ofMessage(message));
				break;

			default:
				break;
			}
		}

		Builder paramsBuilder = ResponseCreateParams.builder()
				.model(chatModel)
				.addTool(GetFunctionTool.class)
				.input(Input.ofResponse(arrayList));

		Response response = client.responses().create(paramsBuilder.build());

		List<ResponseInputItem> inputs = new ArrayList<>();
		response.output().forEach(item -> {
			if (item.isFunctionCall()) {
				ResponseFunctionToolCall functionCall = item.asFunctionCall();

				inputs.add(ResponseInputItem.ofFunctionCall(functionCall));
				try {
					Object callFunction = callFunction(functionCall);
					inputs.add(ResponseInputItem.ofFunctionCallOutput(ResponseInputItem.FunctionCallOutput.builder()
							.callId(functionCall.callId())
							.outputAsJson(callFunction)
							.build()));
				} catch (CommandException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (item.isReasoning()) {
				ResponseInputItem reasoning = ResponseInputItem.ofReasoning(item.asReasoning());
				inputs.add(reasoning);
			}
		});

		if (!inputs.isEmpty()) {
			paramsBuilder.input(ResponseCreateParams.Input.ofResponse(inputs));
			response = client.responses().create(paramsBuilder.build());
		}

		setVariable(name, response);
	}

	private Object callFunction(ResponseFunctionToolCall function) throws CommandException {
		switch (function.name()) {
		case "GetFunctionTool":
			GetFunctionTool arguments = function.arguments(GetFunctionTool.class);
			arguments.setOpenIA(this);
			return arguments.execute();
		default:
			throw new IllegalArgumentException("Unknown function: " + function.name());
		}
	}

	private Message message(String input, String role) {
		String text = replaceProperties(input);
		Message message = com.openai.models.responses.ResponseInputItem.Message
				.builder()
				.role(com.openai.models.responses.ResponseInputItem.Message.Role.of(role))
				.addInputTextContent(text)
				.build();
		return message;
	}

	@CommandDescription("Function command to create a function tool. Property tags define the properties of the function tool. "
			+ "The Task command is called when the model requests the function.")
	@CommandExamples({
			"<Function name='type:string' description='type:string'>"
					+ "<property name='type:string' type='type:string' required='type:boolean'/>"
					+ "<Task>...recipe code...</Task>"
					+ "</Function>" })
	public void runCommandFunction(Node action) {
		String name = attr(action, "name");
		String description = attr(action, "description");

		Node[] props = action.getNodes("property");

		Parameters params = null;
		ObjectMapper mapper = new ObjectMapper();

		Map<String, Map<String, String>> fromValue = new HashMap<>();
		ArrayNode requiregProps = mapper.createArrayNode();
		for (Node propNode : props) {
			String paramName = propNode.getAttribute("name");
			String paramType = propNode.getAttribute("type");
			boolean required = Boolean.parseBoolean(attr(propNode, "required", "false"));
			if (required) {
				requiregProps.add(paramName);
			}

			Map<String, String> value = new HashMap<>();
			value.put("type", paramType);
			fromValue.put(paramName, value);
		}

		JsonValue propsVal = JsonValue.fromJsonNode(mapper.convertValue(fromValue, JsonNode.class));
		JsonValue requiredVal = JsonValue.from(requiregProps);
		params = Parameters.builder()
				.putAdditionalProperty("properties", propsVal)
				.putAdditionalProperty("type", JsonString.of("object"))
				.putAdditionalProperty("required", requiredVal)
				.build();

		com.openai.models.responses.FunctionTool.Builder toolBuilder = FunctionTool.builder()
				.name(name)
				.description(description);

		if (params != null) {
			toolBuilder.parameters(params);
		}

		Tool tool = Tool.ofFunction(toolBuilder.strict(false).build());
		toolsMap.put(tool, action);
	}

	private void setVariable(String name, Response response) {
		List<ResponseOutputItem> output = response.output();
		for (ResponseOutputItem responseOutputItem : output) {
			Optional<ResponseOutputMessage> messageOpt = responseOutputItem.message();
			if (messageOpt.isPresent()) {
				Content content = messageOpt.get().content().get(0);
				String responseText = content.outputText().get().text();
				setVariableValue(name, responseText);
				break;
			}
		}
	}

	public static JsonValue jsonValue(String value) {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode node;
		try {
			node = objectMapper.readTree(value.replace('\'', '\"'));
			return JsonValue.fromJsonNode(node);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@JsonClassDescription("Gets the function tool call result.")
	static class GetFunctionTool {
		@JsonPropertyDescription("The name of function tool.")
		public String name;

		@JsonPropertyDescription("The parameters of function tool in json format.")
		public String params;

		private OpenAI openAI;

		public Object execute() throws CommandException {
			String returnValue = null;
			Set<Entry<Tool, Node>> toolSet = openAI.toolsMap.entrySet();
			for (Entry<Tool, Node> entry : toolSet) {
				Tool tool = entry.getKey();

				if (tool.isFunction()) {
					FunctionTool functionTool = tool.function().get();
					if (functionTool.name().equals(name)) {
						Node functionNode = entry.getValue();
						@SuppressWarnings("unchecked")
						ObjectMapper mapper = new ObjectMapper();
						try {
							JsonNode args = mapper.readTree(params);
							Iterator<String> fieldNames = args.fieldNames();
							while (fieldNames.hasNext()) {
								String name = (String) fieldNames.next();
								String value = args.get(name).asText();
								openAI.setVariableValue(name, value);
							}

						} catch (JsonProcessingException e) {
							throw new IllegalArgumentException("Argument parsing failed. Name: " + name, e);
						}

						String returnName = openAI.attr(functionNode, "return");
						if (returnName != null) {
							openAI.runNodes(functionNode.getNodes("Task"));
							returnValue = openAI.getVariableString(returnName);
						}
						break;
					}
				}
			}

			return returnValue;
		}

		public void setOpenIA(OpenAI openAI) {
			this.openAI = openAI;
		}

	}

}
