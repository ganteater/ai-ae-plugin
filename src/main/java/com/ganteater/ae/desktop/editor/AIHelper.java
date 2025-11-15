package com.ganteater.ae.desktop.editor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.ganteater.ae.processor.BaseProcessor;
import com.ganteater.ae.processor.Processor;
import com.ganteater.ae.processor.annotation.CommandInfo;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

public class AIHelper extends CodeHelper {

	public AIHelper(TextEditor textEditor) throws IOException {
		super(textEditor);

		TaskEditor recipeEditor = getRecipePanel();
		Processor taskProcessor = recipeEditor.getTaskProcessor();
		String apiKey = taskProcessor.attr(recipeEditor.getEditor().getEditorNode(), "apiKey");
		if (apiKey == null) {
			throw new IllegalArgumentException("apiKey attribute required.");
		}
		OpenAIClient client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();

		AIHelperDialog aiHelperDialog = new AIHelperDialog(this, client);
		super.setDefaultDialog(aiHelperDialog);
	}

	public String getExampleContext(Set<String> processorClassNameList) {
		StringBuilder examplesContext = new StringBuilder("# Commands\n");

		for (String processorClassName : processorClassNameList) {

			processorClassName = Processor.getFullClassName(processorClassName);
			@SuppressWarnings("unchecked")
			Class<BaseProcessor> processorClass;
			try {
				processorClass = (Class<BaseProcessor>) Class.forName(processorClassName);

				String name = processorClass.getSimpleName();
				examplesContext.append("## Command Processor: " + name + "\n");
				examplesContext.append("Fully qualified class name: " + processorClass.getName() + "\n");

				List<CommandInfo> commandList = super.getCommandList(null, processorClass);
				for (CommandInfo cominfo : commandList) {

					examplesContext.append("### Command `" + cominfo.getName() + "`\n");
					String description = cominfo.getDescription();
					if (StringUtils.isNotEmpty(description)) {
						examplesContext.append("Description: " + description + "\n");
					}
					List<String> examples = cominfo.getExamples();
					if (!examples.isEmpty()) {
						examplesContext.append("#### Examples\n");
						int i = 1;
						for (String example : examples) {
							example = example.replace("\"", "'");
							examplesContext.append((i++) + ". " + example + "\n");
						}
					}
				}
			} catch (ClassNotFoundException e) {
				examplesContext.append("## Command Processor: " + processorClassName + "\n");
				examplesContext.append("Not found.\n");
			}
		}

		return examplesContext.toString();
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

}
