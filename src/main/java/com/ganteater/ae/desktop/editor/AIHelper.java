package com.ganteater.ae.desktop.editor;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.ganteater.ae.processor.BaseProcessor;
import com.ganteater.ae.processor.Processor;
import com.ganteater.ae.processor.annotation.CommandInfo;
import com.ganteater.ae.util.xml.easyparser.Node;
import com.ganteater.ai.Prompt.Builder;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

public class AIHelper extends CodeHelper {

	private String chatModel;
	private boolean debug;

	public AIHelper(TextEditor textEditor) throws IOException, IllegalAccessException {
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

	public void appendExampleContext(Builder contextBuilder, Collection<String> processorClassNameList) {
		contextBuilder.context("# Commands");

		for (String processorName : processorClassNameList) {
			String processorClassName = Processor.getFullClassName(processorName);

			contextBuilder.context("## Command Processor: " + processorName);
			try {
				@SuppressWarnings("unchecked")
				Class<BaseProcessor> processorClass = (Class<BaseProcessor>) Class.forName(processorClassName);
				contextBuilder.context("Fully qualified class name: " + processorClass.getName());

				List<CommandInfo> commandList = super.getCommandList(null, processorClass);

				for (CommandInfo cominfo : commandList) {
					if (!cominfo.getName().equals("init")) {
						contextBuilder.context("### Command `" + cominfo.getName() + "`");
					} else {
						contextBuilder.context("### Processor Initialization");
					}
					String description = cominfo.getDescription();
					if (StringUtils.isNotEmpty(description)) {
						contextBuilder.context("Description: " + description);
					}
					fillExampes(contextBuilder, cominfo);
				}
			} catch (ClassNotFoundException e) {
				contextBuilder.context("## Command Processor: " + processorName);
				contextBuilder
						.context("This processor can not be used because the processor class: " + processorClassName
								+ " not found. If user tray to use it, need to show the error.");
			}
		}
	}

	private void fillExampes(Builder contextBuilder, CommandInfo cominfo) {
		List<String> examples = cominfo.getExamples();
		if (!examples.isEmpty()) {
			contextBuilder.context("#### Examples");
			int i = 1;
			StringBuilder examplesInfo = new StringBuilder();
			for (String example : examples) {
				appendExample(examplesInfo, i++, example);
			}
			contextBuilder.context(examplesInfo.toString());
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

	public void appendSystemVariablesContext(Builder promptBuilder) {
		Map<String, Object> startVariables = getRecipePanel().getManager().getSystemVariables();
		promptBuilder.context("# System Variable Names");

		Set<String> keySet = startVariables.keySet();
		int i = 1;
		StringBuilder sysvarInfo = new StringBuilder();
		for (String name : keySet) {
			sysvarInfo.append((i++) + ". " + name + "\n");
		}
		promptBuilder.context(sysvarInfo.toString());
	}

	public String getChatModel() {
		return chatModel;
	}

	public boolean isDebug() {
		return debug;
	}

}
