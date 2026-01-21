package com.ganteater.ae.desktop.editor;

import java.io.IOException;

import com.ganteater.ae.processor.CodeMie;
import com.ganteater.ae.processor.Processor;
import com.ganteater.ae.util.xml.easyparser.Node;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

public class CodeMieHelper extends AICodeHelper {

	public CodeMieHelper(TextEditor textEditor) throws IOException, IllegalAccessException {
		super(textEditor);
	}

	@Override
	protected OpenAIClient createClient(Processor taskProcessor, Node editorNode) throws IOException {
		String username = taskProcessor.attr(editorNode, "username");
		String password = taskProcessor.attr(editorNode, "password");
		
		String apiKey = CodeMie.getToken(username, password);
		OpenAIClient client = OpenAIOkHttpClient.builder().apiKey(apiKey).baseUrl(CodeMie.baseUrl).build();
		return client;
	}
}
