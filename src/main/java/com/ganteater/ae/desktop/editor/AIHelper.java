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

public class AIHelper extends CodeHelper {

	private String chatModel;

	public AIHelper(TextEditor textEditor) throws IOException, IllegalAccessException {
		super(textEditor);

		TaskEditor recipeEditor = getRecipePanel();
		Processor taskProcessor = recipeEditor.getTaskProcessor();

		Node editorNode = recipeEditor.getEditor().getEditorNode();

		chatModel = StringUtils.defaultIfEmpty(taskProcessor.attr(editorNode, "model"), "gpt-4.1-mini");

		String apiKey = taskProcessor.attr(editorNode, "apiKey");
		if (apiKey == null) {
			throw new IllegalArgumentException("apiKey attribute required.");
		}

		OpenAIClient client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();

		AIHelperDialog aiHelperDialog = new AIHelperDialog(this, client);
		super.setDefaultDialog(aiHelperDialog);
	}

	public String getExampleContext(Set<String> processorClassNameList) {
		StringBuilder examplesContext = new StringBuilder("# Commands\n\n");

		for (String processorName : processorClassNameList) {
			String processorClassName = Processor.getFullClassName(processorName);

			StringBuilder context = new StringBuilder("## Command Processor: " + processorName + "\n\n");
			try {
				@SuppressWarnings("unchecked")
				Class<BaseProcessor> processorClass = (Class<BaseProcessor>) Class.forName(processorClassName);
				context.append("Fully qualified class name: " + processorClass.getName() + "\n\n");

				List<CommandInfo> commandList = super.getCommandList(null, processorClass);

				for (CommandInfo cominfo : commandList) {
					if (!cominfo.getName().equals("init")) {
						context.append("### Command `" + cominfo.getName() + "`\n\n");
					} else {
						context.append("### Processor Initialization\n\n");
					}
					String description = cominfo.getDescription();
					if (StringUtils.isNotEmpty(description)) {
						context.append("Description: " + description + "\n\n");
					}
					fillExampes(context, cominfo);
				}
			} catch (ClassNotFoundException e) {
				context.append("## Command Processor: " + processorName + "\n\n");
				context.append("This processor can not be used because the processor class: " + processorClassName
						+ " not found. If user tray to use it, need to show the error.\n");
			}
			context.append("\n");
			examplesContext.append(context);
		}

		return examplesContext.toString();

	}

	private void fillExampes(StringBuilder context, CommandInfo cominfo) {
		List<String> examples = cominfo.getExamples();
		if (!examples.isEmpty()) {
			context.append("#### Examples\n\n");
			int i = 1;
			for (String example : examples) {
				appendExample(context, i++, example);
			}
		}
		context.append("\n");
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

		example = example.replace("'", "\"");
		if (StringUtils.contains(example, "\n")) {
			context.append(description + "\n"
					+ "```xml\n" + code + "\n```\n");
		} else {
			context.append("- " + description + "`" + code + "`\n");
		}
	}

	public String getSystemVariablesContext() {
		Map<String, Object> startVariables = getRecipePanel().getManager().getSystemVariables();
		StringBuilder examplesContext = new StringBuilder("# System Variable Names\n\n");

		Set<String> keySet = startVariables.keySet();
		int i = 1;
		for (String name : keySet) {
			examplesContext.append((i++) + ". " + name + "\n");
		}
		examplesContext.append("\n");
		return examplesContext.toString();
	}

	public String getChatModel() {
		return chatModel;
	}

}
