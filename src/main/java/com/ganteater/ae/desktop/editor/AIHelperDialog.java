package com.ganteater.ae.desktop.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ganteater.ae.AELogRecord;
import com.ganteater.ae.ILogger;
import com.ganteater.ae.desktop.ui.OptionPane;
import com.ganteater.ae.desktop.view.View;
import com.ganteater.ae.processor.Processor;
import com.ganteater.ae.util.AEUtils;
import com.ganteater.ae.util.ClassUtils;
import com.ganteater.ai.Marker;
import com.ganteater.ai.MarkerExtractResult;
import com.ganteater.ai.Prompt;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.errors.RateLimitException;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseCreateParams.Builder;
import com.openai.models.responses.ResponseCreateParams.Input;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseInputItem.Message;
import com.openai.models.responses.ResponseOutputMessage.Content;
import com.openai.models.responses.ResponseUsage;

public class AIHelperDialog extends HelperDialog {

	private static final long serialVersionUID = 1L;

	private static final String REQUEST_BUTTON_TEXT = "Perform";
	private static Map<String, ResponseInputItem> contextMap = new LinkedHashMap<>();

	private ILogger log;
	private JTextArea editor = new JTextArea();
	private JButton perform = new JButton(REQUEST_BUTTON_TEXT);

	public AIHelperDialog(final AICodeHelper codeHelper, final OpenAIClient client) {
		super(codeHelper);

		setAlwaysOnTop(true);
		setUndecorated(true);

		editor.setTabSize(2);
		editor.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		editor.setLineWrap(true);
		editor.setWrapStyleWord(true);

		JScrollPane comp = new JScrollPane(editor);
		comp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		comp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		comp.setPreferredSize(new Dimension(300, 150));

		String appendSystemVariablesContext = getCodeHelper().appendSystemVariablesContext();
		addContextInput("SystemVariablesContext", appendSystemVariablesContext);

		List<Class<?>> processorClasses = ClassUtils.findAssignable(Processor.class);
		for (Class<?> processorClass : processorClasses) {
			String input = codeHelper.appendProcessorInfo(processorClass);
			addContextInput(processorClass.getName(), input);
		}

		List<Class<?>> viewNames = ClassUtils.findAssignable(View.class);
		for (Class<?> viewName : viewNames) {
			String input = codeHelper.appendViewInfo(viewName);
			addContextInput(viewName.getName(), input);
		}

		getContentPane().add(comp, BorderLayout.CENTER);
		getContentPane().add(perform, BorderLayout.SOUTH);

		perform.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				perform(client);
			}
		});

		editor.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE:
					setVisible(false);
					break;
				case KeyEvent.VK_ENTER:
					if (!e.isShiftDown()) {
						perform(client);
						e.consume();
					} else {
						editor.insert("\n", editor.getCaretPosition());
					}
					break;
				}
			}
		});

		editor.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				Component cmp = e.getOppositeComponent();
				if (cmp != AIHelperDialog.this && cmp != editor && cmp != perform) {
					setVisible(false);
				}
			}
		});

	}

	private void perform(final OpenAIClient client) {
		new Thread(() -> {
			try {
				perform.setEnabled(false);
				perform.setText("Waiting for the response ...");
				performRequest(client);
				setVisible(false);

			} finally {
				perform.setText(REQUEST_BUTTON_TEXT);
				perform.setEnabled(true);
			}
		}).start();
	}

	private void addContextInput(String name, String processorInfo) {
		Message message = com.openai.models.responses.ResponseInputItem.Message
				.builder()
				.role(com.openai.models.responses.ResponseInputItem.Message.Role.SYSTEM)
				.addInputTextContent(processorInfo)
				.build();

		contextMap.put(name, ResponseInputItem.ofMessage(message));
	}

	protected void performRequest(OpenAIClient client) {
		try {
			TextEditor textEditor = getCodeHelper().getEditor();

			int caretPosition = textEditor.getCaretPosition();
			int selectionStart = textEditor.getSelectionStart();
			int selectionEnd = textEditor.getSelectionEnd();

			List<ResponseInputItem> inputs = new ArrayList<>();

			Collection<ResponseInputItem> values = contextMap.values();
			for (ResponseInputItem processorInfo : values) {
				String text = processorInfo.message().get().content().get(0).inputText().get().text();
				debug(new AELogRecord(text, "md", "Input"));
				inputs.add(processorInfo);
			}

			String text = textEditor.getText();
			Prompt prompt = new Prompt.Builder()
					.source(text, "xml", caretPosition, selectionStart, selectionEnd)
					.input(editor.getText())
					.build();

			String input = prompt.buildPrompt();
			debug(new AELogRecord(input, "md", "Input"));

			Message message = com.openai.models.responses.ResponseInputItem.Message
					.builder()
					.role(com.openai.models.responses.ResponseInputItem.Message.Role.USER)
					.addInputTextContent(input)
					.build();

			inputs.add(ResponseInputItem.ofMessage(message));

			String chatModel = getCodeHelper().getChatModel();
			Builder builder = ResponseCreateParams.builder()
					.model(chatModel)
					.input(Input.ofResponse(inputs));

			Response response = client.responses().create(builder.build());
			logUsage(response.usage());

			response.output().forEach(item -> {
				if (item.isMessage()) {
					List<Content> content = item.asMessage().content();
					performMessage(content);
				}
			});

		} catch (RateLimitException e) {
			OptionPane.showMessageDialog(getCodeHelper().getRecipePanel().getFrame(), e.getLocalizedMessage(),
					"Rate Limit", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void performMessage(List<Content> content) {
		String responseText = content.get(0).outputText().get().text();
		debug(new AELogRecord(responseText, "xml", "Output"));

		getCodeHelper().hide();
		updateCode(responseText);
	}

	private void updateCode(String responseText) {
		String code = StringUtils.substringBetween(responseText, "```xml\n", "```");
		if (code == null) {
			code = responseText;
		}

		MarkerExtractResult mextract = Marker.extractAll(code);
		int cursor = mextract.getPosition(Marker.CURSOR);
		int start = mextract.getPosition(Marker.SELECTION_START);
		int end = mextract.getPosition(Marker.SELECTION_END);

		TextEditor textEditor = getCodeHelper().getEditor();
		textEditor.setText(mextract.getText());
		if (StringUtils.isNotBlank(code)) {
			textEditor.getRecipePanel().compileTask();

			TaskEditor recipePanel = getCodeHelper().getEditor().getRecipePanel();
			recipePanel.compileTask();
			recipePanel.refreshTaskTree();
			try {
				textEditor.setCaretPosition(cursor < 0 ? textEditor.getCaretPosition() : cursor);
				if (start > 0) {
					textEditor.select(start, end);
				}
			} catch (IllegalArgumentException e1) {
				textEditor.setCaretPosition(code.length());
			}

		}
	}

	private void logUsage(Optional<ResponseUsage> optional) {
		if (optional.isPresent()) {
			ResponseUsage responseUsage = optional.get();
			long inputTokens = responseUsage.inputTokens();
			long inputCachedTokens = optional.get().inputTokensDetails().cachedTokens();
			long outputTokens = optional.get().outputTokens();
			long reasoningTokens = optional.get().outputTokensDetails().reasoningTokens();

			debug(String.format("Input: %1$d, cached: %2$d, output: %3$d, reasoning: %4$d tokens.",
					inputTokens, inputCachedTokens, outputTokens, reasoningTokens));
		}
	}

	private void debug(Object message) {
		if (getCodeHelper().isDebug()) {
			getLog().debug(message);
		}
	}

	public static JsonValue jsonValue(String value) {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode node;
		try {
			node = objectMapper.readTree(value.replace('\'', '\"'));
			return JsonValue.fromJsonNode(node);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public Object getProcessorDescription(Object processorName) {
		return null;
	}

	private ILogger getLog() {
		log = getCodeHelper().getEditor().getRecipePanel().getLogger();
		if (log == null) {
			log = getCodeHelper().getRecipePanel().createLog("Helper", true);
		}
		return log;
	}

	@Override
	public void showDialog() {
		super.showDialog();
		SwingUtilities.invokeLater(() -> {
			editor.requestFocusInWindow();
		});
	}

	@Override
	public AICodeHelper getCodeHelper() {
		return (AICodeHelper) super.getCodeHelper();
	}

}
