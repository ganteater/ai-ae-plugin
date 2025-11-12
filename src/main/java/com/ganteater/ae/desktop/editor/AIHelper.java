package com.ganteater.ae.desktop.editor;

import com.ganteater.ae.processor.Processor;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

public class AIHelper extends CodeHelper {

	private static OpenAIClient client;

	public AIHelper(TextEditor recipeEditor) {
		super(recipeEditor);

	}

	@Override
	protected HelperDialog createPopup() {
		TaskEditor recipeEditor = getRecipePanel();
		Processor taskProcessor = recipeEditor.getTaskProcessor();
		String apiKey = taskProcessor.attr(recipeEditor.getEditor().getEditorNode(), "apiKey");
		if (apiKey == null) {
			throw new IllegalArgumentException("apiKey attribut required.");
		}
		client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();
		return new AIHelperDialog(this, client);
	}

}
