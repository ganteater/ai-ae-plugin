package com.ganteater.ae.desktop.editor;

import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.ganteater.ae.processor.BaseProcessor;
import com.ganteater.ae.processor.Processor;
import com.ganteater.ae.util.xml.easyparser.EasyParser;
import com.ganteater.ae.util.xml.easyparser.Node;
import com.ganteater.ai.Marker;
import com.ganteater.ai.MarkerExtractResult;
import com.ganteater.ai.Prompt;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseCreateParams.Builder;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.services.blocking.ResponseService;

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
					new Thread(() -> performRequest(client)).start();
					setVisible(false);
					break;
				}
			}
		});
		
		editor.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (e.getOppositeComponent() != AIHelperDialog.this && e.getOppositeComponent() != editor) {
					setVisible(false);
				}
			}
		});
	}

	protected void performRequest(OpenAIClient client) {
		String text = editor.getText();

		try {
			TextEditor textEditor = getCodeHelper().getEditor();

			int caretPosition = textEditor.getCaretPosition();
			int selectionStart = textEditor.getSelectionStart();
			int selectionEnd = textEditor.getSelectionEnd();

			ResponseService responses = client.responses();

			Prompt.Builder promptBuilder = new Prompt.Builder();

			Set<Class<BaseProcessor>> processorClassList = new HashSet<Class<BaseProcessor>>();
			processorClassList.add(BaseProcessor.class);
			Node taskNode = new EasyParser().getObject(textEditor.getRecipePanel().getEditor().getText());
			Node[] nodes = taskNode.getNodes("Extern");
			for (Node node : nodes) {
				try {
					String processorClassName = node.getAttribute("class");
					processorClassName = Processor.getFullClassName(processorClassName);
					Class<BaseProcessor> forName = (Class<BaseProcessor>) Class.forName(processorClassName);
					processorClassList.add(forName);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			String examplesContext = ((AIHelper) getCodeHelper()).getExampleContext(processorClassList);

			promptBuilder.setContext(examplesContext)
					.setSource(textEditor.getText(), caretPosition, selectionStart, selectionEnd)
					.setHint("response should have only recipe text without any additional texts.").setInput(text);

			promptBuilder.build().apply(p -> {
				Builder builder = ResponseCreateParams.builder();

				ResponseCreateParams params = builder.temperature(0.7).input(p).model(ChatModel.GPT_4_1).build();

				Response response = responses.create(params);
				List<ResponseOutputItem> output = response.output();
				ResponseOutputMessage message = output.get(0).message().get();
				String responseText = message.content().get(0).outputText().get().text();

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

				textEditor.setText(mextract.getText());
				if (StringUtils.isNotBlank(code)) {
					textEditor.getRecipePanel().compileTask();

					TaskEditor recipePanel = getCodeHelper().getEditor().getRecipePanel();
					recipePanel.compileTask();
					recipePanel.refreshTaskTree();
					try {
						textEditor.setCaretPosition(cursor < 0 ? caretPosition : cursor);
						textEditor.select(start, end);
					} catch (IllegalArgumentException e1) {
						textEditor.setCaretPosition(code.length());
					}

				}
			});

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
