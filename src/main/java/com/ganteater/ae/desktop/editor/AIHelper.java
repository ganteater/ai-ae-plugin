package com.ganteater.ae.desktop.editor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
		StringBuilder examplesContext = new StringBuilder("# Commands\n");

		for (String processorName : processorClassNameList) {
			String processorClassName = Processor.getFullClassName(processorName);

			StringBuilder context = new StringBuilder("## Command Processor: " + processorName + "\n");
			try {
				@SuppressWarnings("unchecked")
				Class<BaseProcessor> processorClass = (Class<BaseProcessor>) Class.forName(processorClassName);
				context.append("Fully qualified class name: " + processorClass.getName() + "\n");

				List<CommandInfo> commandList = super.getCommandList(null, processorClass);

				for (CommandInfo cominfo : commandList) {
					context.append("### Command `" + cominfo.getName() + "`\n");
					String description = cominfo.getDescription();
					if (StringUtils.isNotEmpty(description)) {
						context.append("Description: " + description + "\n");
					}
					fillExampes(context, cominfo);
				}
			} catch (ClassNotFoundException e) {
				context.append("## Command Processor: " + processorName + "\n");
				context.append("This processor can not be used because the processor class: " + processorClassName
						+ " not found. If user tray to use it, need to show the error.\n");
			}
			examplesContext.append(context);
		}

		return examplesContext.toString();

	}

	private void fillExampes(StringBuilder context, CommandInfo cominfo) {
		List<String> examples = cominfo.getExamples();
		if (!examples.isEmpty()) {
			context.append("#### Examples\n");
			int i = 1;
			for (String example : examples) {
				example = example.replace("'", "\"");
				context.append((i++) + ". " + example + "\n");
			}
		}
	}

	public String getSystemVariablesContext() {
		Map<String, Object> startVariables = getRecipePanel().getManager().getSystemVariables();
		StringBuilder examplesContext = new StringBuilder("# System Variable Names\n");

		Set<String> keySet = startVariables.keySet();
		int i = 1;
		for (String name : keySet) {
			examplesContext.append((i++) + ". " + name + "\n");
		}
		return examplesContext.toString();
	}

	public String getChatModel() {
		return chatModel;
	}

}
