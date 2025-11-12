package com.ganteater.ae.desktop.editor;

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;

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

					String webpageUrl = "https://ganteater.com/commands.md";
					try {
						String relevantText = extractTextFromHtml(webpageUrl);

						TextEditor textEditor = getCodeHelper().getEditor();
						StringBuilder textWithCursor = new StringBuilder(textEditor.getText());
						int caretPosition = textEditor.getCaretPosition();
						textWithCursor.insert(caretPosition, "[CURSOR]");
						int selectionStart = textEditor.getSelectionStart();
						if (selectionStart < caretPosition) {
							textWithCursor.insert(selectionStart - 1, "[SELECTION_START]");
						} else {
							textWithCursor.insert(selectionStart + "[CURSOR]".length() - 1, "[SELECTION_START]");
						}

						int selectionEnd = textEditor.getSelectionEnd();
						if (selectionEnd < caretPosition) {
							textWithCursor.insert(selectionEnd + 1, "[SELECTION_END]");
						} else {
							textWithCursor.insert(selectionEnd + "[CURSOR]".length() + "[SELECTION_END] + 1".length(),
									"[SELECTION_END]");
						}

						ResponseCreateParams params = ResponseCreateParams.builder()
								.input("Here is information from a webpage: " + relevantText
										+ "\n\nCurrect recipe code, if a part of code selected then [SELECTION_START] and [SELECTION_END] to use mark that and place the [CURSOR] position below:\n"
										+ textWithCursor.toString() + "\n\nBased on this, answer my question: \"" + text
										+ "\"Output format hint: response should have only recipe code without any additional text.")
								.model(ChatModel.GPT_4_1).build();

						Response response = client.responses().create(params);
						List<ResponseOutputItem> output = response.output();
						ResponseOutputMessage message = output.get(0).message().get();
						String responseText = message.content().get(0).outputText().get().text();

						System.out.println(responseText);

						int cursor = StringUtils.indexOf(responseText, "[CURSOR]");
						if (cursor > 0) {
							responseText = responseText.replace("[CURSOR]", "");
							caretPosition = cursor;
						}

						getCodeHelper().hide();

						String code = StringUtils.substringBetween(responseText, "```xml\n", "```");
						if (code == null) {
							code = responseText;
						}
						getCodeHelper().getEditor().setText(code);
						if (StringUtils.isNotBlank(code)) {
							getCodeHelper().getEditor().getRecipePanel().compileTask();
							try {
								textEditor.setCaretPosition(caretPosition);
							} catch (IllegalArgumentException e1) {
								textEditor.setCaretPosition(code.length());
							}
						}
						setVisible(false);

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
