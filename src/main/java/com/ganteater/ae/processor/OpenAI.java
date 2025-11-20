package com.ganteater.ae.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import com.ganteater.ae.CommandException;
import com.ganteater.ae.processor.annotation.CommandDescription;
import com.ganteater.ae.processor.annotation.CommandExamples;
import com.ganteater.ae.util.xml.easyparser.Node;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseCreateParams.Builder;
import com.openai.models.responses.ResponseCreateParams.Input;
import com.openai.models.responses.ResponseInputItem.Message;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputMessage.Content;
import com.openai.services.blocking.ResponseService;

public class OpenAI extends BaseProcessor {

	private static final String DEFAULT_MODEL_NAME = "gpt-5-mini";
	private OpenAIClient client;
	private String temperatureStr;
	private String chatModel;

	@Override
	@CommandDescription("OpenAI processor supports command to call OpenAI services.")
	@CommandExamples({
			"<Extern class='OpenAI'  model='enum:gpt-5|gpt-5-mini' apiKey='type:string' temperature='type:float:0...1>" })
	public void init(Processor parentProcessor, Node action) throws CommandException {
		super.init(parentProcessor, action);
		chatModel = attr(action, "model", DEFAULT_MODEL_NAME);

		String apiKey = attr(action, "apiKey");
		if (StringUtils.isBlank(apiKey)) {
			throw new CommandException("apiKey is required.", parentProcessor);
		}

		temperatureStr = attr(action, "temperature");
		client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();
	}

	@CommandDescription("The 'name' attribute is used to define the property name where the response will be stored.")
	@CommandExamples({ "<Prompt name='type:property'>...</Prompt>" })
	public void runCommandPrompt(Node action) {
		String name = action.getAttribute("name");
		String input = replaceProperties(action.getInnerText());
		String chatModel = attr(action, "model", DEFAULT_MODEL_NAME);

		ResponseService responses = client.responses();
		Builder builder = ResponseCreateParams.builder()
				.model(chatModel)
				.input(input);

		if (temperatureStr != null) {
			builder.temperature(Float.parseFloat(temperatureStr));
		}

		ResponseCreateParams params = builder.build();

		Response response = responses.create(params);
		setVariable(name, response);
	}

	@CommandDescription("The 'name' attribute is used to define the property name where the response will be stored.")
	@CommandExamples({
			"<Messages name='type:property'><message role='enum:system|developer|user'>...</message></Prompt>" })
	public void runCommandMessages(Node action) {
		String name = action.getAttribute("name");
		ResponseService responses = client.responses();

		ArrayList<ResponseInputItem> arrayList = new ArrayList<ResponseInputItem>();
		for (Node node : action) {
			switch (node.getTag()) {
			case "message":
				String text = replaceProperties(node.getInnerText());
				String role = StringUtils.defaultIfEmpty(node.getAttribute("role"), "USER");
				Message message = com.openai.models.responses.ResponseInputItem.Message
						.builder()
						.role(com.openai.models.responses.ResponseInputItem.Message.Role.of(role))
						.addInputTextContent(text)
						.build();

				arrayList.add(ResponseInputItem.ofMessage(message));
				break;

			default:
				break;
			}
		}

		ResponseCreateParams params = ResponseCreateParams.builder()
				.model(chatModel)
				.input(Input.ofResponse(arrayList))
				.build();

		Response response = responses.create(params);
		setVariable(name, response);
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
}
