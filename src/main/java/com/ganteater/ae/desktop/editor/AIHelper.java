package com.ganteater.ae.desktop.editor;

import java.io.IOException;

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

}
