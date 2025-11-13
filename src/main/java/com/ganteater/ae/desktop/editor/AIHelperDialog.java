package com.ganteater.ae.desktop.editor;

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.ganteater.ai.Marker;
import com.ganteater.ai.MarkerExtractResult;
import com.ganteater.ai.Prompt;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.services.blocking.chat.ChatCompletionService;

public class AIHelperDialog extends HelperDialog {

	private JTextArea editor = new JTextArea();

	public AIHelperDialog(final CodeHelper codeHelper, final OpenAIClient client) {
		super(codeHelper);

		setAlwaysOnTop(true);
		setUndecorated(true);

		editor.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		editor.setLineWrap(true);
		editor.setWrapStyleWord(true);

		JScrollPane comp = new JScrollPane(editor);
		comp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		comp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		comp.setPreferredSize(new Dimension(300, 200));

		getContentPane().add(comp);

		editor.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE:
					setVisible(false);
					break;
				case KeyEvent.VK_ENTER:
					String text = editor.getText();

					try (InputStream context = AIHelperDialog.class.getClassLoader()
							.getResourceAsStream("commands.md")) {
						String relevantText = IOUtils.toString(context);
						TextEditor textEditor = getCodeHelper().getEditor();

						int caretPosition = textEditor.getCaretPosition();
						int selectionStart = textEditor.getSelectionStart();
						int selectionEnd = textEditor.getSelectionEnd();

						ChatCompletionService chat = client.chat().completions();

						Prompt.Builder promptBuilder = new Prompt.Builder();
						promptBuilder.setContext(relevantText)
								.setSource(textEditor.getText(), caretPosition, selectionStart, selectionEnd)
								.setHint("response should have only recipe text without any additional texts.")
								.setInput(text);

						promptBuilder.build().apply(p -> {
							com.openai.models.chat.completions.ChatCompletionCreateParams.Builder builder = ChatCompletionCreateParams
									.builder();

							ChatCompletionCreateParams params = builder.temperature(0.7).addAssistantMessage(p)
									.model(ChatModel.GPT_4_1).build();

							ChatCompletion response = chat.create(params);
							String responseText = response.choices().get(0).message().content().get();

							System.out.println(responseText);

							getCodeHelper().hide();

							String code = StringUtils.substringBetween(responseText, "```xml\n", "```");
							if (code == null) {
								code = responseText;
							}

							MarkerExtractResult mextract = Marker.extractAll(code);
							int cursor = mextract.getPosition(Marker.CURSOR);
							int start = mextract.getPosition(Marker.SELECTION_START);
							int end = mextract.getPosition(Marker.SELECTION_END);

							getCodeHelper().getEditor().setText(mextract.getText());
							if (StringUtils.isNotBlank(code)) {

								getCodeHelper().getEditor().getRecipePanel().compileTask();
								try {
									textEditor.setCaretPosition(cursor < 0 ? caretPosition : cursor);
									textEditor.select(start, end);
								} catch (IllegalArgumentException e1) {
									textEditor.setCaretPosition(code.length());
								}
							}
							setVisible(false);
						});

					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					break;
				}
			}
		});
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void showDialog() {
		super.showDialog();
		SwingUtilities.invokeLater(() -> {
			editor.requestFocusInWindow();
		});
	}

	public static String extractTextFromHtml(String urlString) {
		Document doc;
		try {
			doc = Jsoup.parse(new URL(urlString), 5000);
			return doc.body().text();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
