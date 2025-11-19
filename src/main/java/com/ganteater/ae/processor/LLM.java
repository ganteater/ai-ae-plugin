package com.ganteater.ae.processor;

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
import com.openai.models.responses.ResponseCreateParams.Builder;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.services.blocking.ResponseService;

public class LLM extends BaseProcessor {

	private OpenAIClient client;
	private String temperatureStr;

	@Override
	@CommandDescription("LLM processor supports command to call services like OpenAI.")
	@CommandExamples({ "<Extern class='LLM' apiKey='type:string' temperature='1'>" })
	public void init(Processor parentProcessor, Node action) throws CommandException {
		super.init(parentProcessor, action);

		String apiKey = attr(action, "apiKey");
		if (StringUtils.isBlank(apiKey)) {
			throw new CommandException("apiKey is required.", parentProcessor);
		}

		temperatureStr = attr(action, "temperature");
		client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();
	}

	@CommandDescription("The 'name' attribute is used to define the property name where the response will be stored.")
	@CommandExamples({ "<Prompt name='type:property' model='enum:gpt-5|gpt-5'>...</Prompt>" })
	public void runCommandPrompt(Node action) {
		String input = replaceProperties(action.getInnerText());
		String chatModel = attr(action, "model", "gpt-5-mini");

		ResponseService responses = client.responses();
		Builder builder = ResponseCreateParams.builder()
				.model(chatModel)
				.input(input);

		if (temperatureStr != null) {
			builder.temperature(Double.parseDouble(temperatureStr));
		}

		ResponseCreateParams params = builder.build();

		Response response = responses.create(params);
		List<ResponseOutputItem> output = response.output();
		ResponseOutputItem responseOutputItem = output.get(0);
		Optional<ResponseOutputMessage> messageOpt = responseOutputItem.message();
		if (messageOpt.isPresent()) {
			ResponseOutputMessage message = messageOpt.get();
			String responseText = message.content().get(0).outputText().get().text();
			setVariableValue(action.getAttribute("name"), responseText);
		}
	}
}
