package com.ganteater.ae.desktop.editor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ganteater.ae.processor.BaseProcessor;
import com.ganteater.ae.processor.Processor;
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

	public String getExampleContext(Set<Class<BaseProcessor>> processorClassList) {
		StringBuilder examplesContext = new StringBuilder("# Commands\n");
		for (Class<BaseProcessor> processorClass : processorClassList) {
			examplesContext.append("## Command List of " + processorClass.getName() + "\n");
			Map<Class<? extends Processor>, List<String>> commandList = super.getCommandList(null, processorClass);
			Map<String, List<String>> commandExamples = getCommandExamples(commandList);
			Set<Entry<String, List<String>>> entrySet = commandExamples.entrySet();

			for (Entry<String, List<String>> entry : entrySet) {
				String commandName = entry.getKey();
				examplesContext.append("### " + commandName + "\n");

				List<String> examples = entry.getValue();
				if (!examples.isEmpty()) {
					examplesContext.append("#### Examples\n");
					int i = 1;
					for (String example : examples) {
						examplesContext.append((i++) + ". " + example + "\n");
					}
				}
			}
		}

		return examplesContext.toString();
	}

}
