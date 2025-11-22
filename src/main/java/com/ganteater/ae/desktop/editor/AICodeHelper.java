package com.ganteater.ae.desktop.editor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.ganteater.ae.processor.BaseProcessor;
import com.ganteater.ae.processor.Processor;
import com.ganteater.ae.processor.annotation.CommandInfo;
import com.ganteater.ae.util.xml.easyparser.Node;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

public class AICodeHelper extends CodeHelper {

	private String chatModel;
	private boolean debug;

	public AICodeHelper(TextEditor textEditor) throws IOException, IllegalAccessException {
		super(textEditor);

		TaskEditor recipeEditor = getRecipePanel();
		Processor taskProcessor = recipeEditor.getTaskProcessor();

		Node editorNode = recipeEditor.getEditor().getEditorNode();

		chatModel = StringUtils.defaultIfEmpty(taskProcessor.attr(editorNode, "model"), "gpt-5-mini");

		debug = Boolean.parseBoolean(taskProcessor.attr(editorNode, "debug", "false"));

		String apiKey = taskProcessor.attr(editorNode, "apiKey");
		if (apiKey == null) {
			throw new IllegalArgumentException("apiKey attribute required.");
		}

		OpenAIClient client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();

		AIHelperDialog aiHelperDialog = new AIHelperDialog(this, client);
		super.setDefaultDialog(aiHelperDialog);
	}

	public String appendExampleContext(String processorName) {
		StringBuilder contextBuilder = new StringBuilder();
		String processorClassName = Processor.getFullClassName(processorName);

		contextBuilder.append("# Command Processor: " + processorName + "\n\n");
		try {
			@SuppressWarnings("unchecked")
			Class<BaseProcessor> processorClass = (Class<BaseProcessor>) Class.forName(processorClassName);
			contextBuilder.append("Fully qualified class name: " + processorClass.getName() + "\n\n");

			List<CommandInfo> commandList = super.getCommandList(null, processorClass);

			for (CommandInfo cominfo : commandList) {
				if (!cominfo.getName().equals("init")) {
					contextBuilder.append("## Command `" + cominfo.getName() + "`n\n");
				} else {
					contextBuilder.append("## Processor Initialization\n\n");
				}
				String description = cominfo.getDescription();
				if (StringUtils.isNotEmpty(description)) {
					contextBuilder.append("Description: " + description + "\n\n");
				}
				fillExampes(contextBuilder, cominfo);
			}
		} catch (ClassNotFoundException e) {
			contextBuilder.append("# Command Processor: " + processorName + "\n\n");
			contextBuilder
					.append("This processor can not be used because the processor class: " + processorClassName
							+ " not found. If user tray to use it, need to show the error.\n\n");
		}

		return contextBuilder.toString();
	}

	private void fillExampes(StringBuilder builder, CommandInfo cominfo) {
		List<String> examples = cominfo.getExamples();
		if (!examples.isEmpty()) {
			builder.append("Examples:\n\n");
			int i = 1;
			StringBuilder examplesInfo = new StringBuilder();
			for (String example : examples) {
				appendExample(examplesInfo, i++, example);
			}
			builder.append(examplesInfo.toString());
		}
	}

	private void appendExample(StringBuilder context, int i, String example) {
		String code = example;
		String description = "";
		int colonIndex = StringUtils.indexOf(example, ':');
		int startTagIndex = StringUtils.indexOf(example, '<');
		if (colonIndex >= 0 && colonIndex < startTagIndex) {
			code = StringUtils.substring(example, colonIndex + 1);
			description = StringUtils.substring(example, 0, colonIndex + 1);
		}

		code = code.replace("'", "\"");
		if (StringUtils.contains(example, "\n")) {
			context.append(description + "\n"
					+ "```xml\n" + code + "\n```\n");
		} else {
			context.append("- " + description + "`" + code + "`\n");
		}
	}

	public String appendSystemVariablesContext() {
		Map<String, Object> startVariables = getRecipePanel().getManager().getSystemVariables();
		StringBuilder builder = new StringBuilder();
		builder.append("# System Variable Names\n\n");

		Set<String> keySet = startVariables.keySet();
		int i = 1;
		StringBuilder sysvarInfo = new StringBuilder();
		for (String name : keySet) {
			sysvarInfo.append((i++) + ". " + name + "\n");
		}
		builder.append(sysvarInfo.toString() + "\n\n");
		return builder.toString();
	}

	public String getChatModel() {
		return chatModel;
	}

	public boolean isDebug() {
		return debug;
	}

}
