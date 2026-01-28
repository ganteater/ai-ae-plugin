package com.ganteater.ae.desktop.editor;

import java.io.IOException;

import com.ganteater.ae.processor.CodeMie;
import com.ganteater.ae.processor.Processor;
import com.ganteater.ae.util.xml.easyparser.Node;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

/**
 * {@link AICodeHelper} specialization that authenticates using CodeMie credentials.
 *
 * <p>
 * The editor node is expected to provide {@code username} and {@code password} attributes; an access token is
 * retrieved via {@link CodeMie#getToken(String, String)} and then used as the API key for the OpenAI client.
 * </p>
 */
public class CodeMieHelper extends AICodeHelper {

	public CodeMieHelper(TextEditor textEditor) throws IOException, IllegalAccessException {
		super(textEditor);
	}

	@Override
	protected OpenAIClient createClient(Processor taskProcessor, Node editorNode) throws IOException {
		String username = taskProcessor.attr(editorNode, "username");
		String password = taskProcessor.attr(editorNode, "password");

		String apiKey = CodeMie.getToken(username, password);
		return OpenAIOkHttpClient.builder().apiKey(apiKey).baseUrl(CodeMie.baseUrl).build();
	}
}
