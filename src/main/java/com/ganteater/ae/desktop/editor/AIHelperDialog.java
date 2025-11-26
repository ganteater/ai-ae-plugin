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
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.ganteater.ae.AELogRecord;
import com.ganteater.ae.ILogger;
import com.ganteater.ae.desktop.ui.OptionPane;
import com.ganteater.ae.desktop.view.View;
import com.ganteater.ae.processor.Processor;
import com.ganteater.ae.util.AEUtils;
import com.ganteater.ae.util.ClassUtils;
import com.ganteater.ai.model.CommandProcessorInfo;
import com.ganteater.ai.model.Editor;
import com.ganteater.ai.model.Selection;
import com.ganteater.ai.model.VariableReport;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.errors.RateLimitException;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseCreateParams.Builder;
import com.openai.models.responses.ResponseCreateParams.Input;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseInputItem.Message;
import com.openai.models.responses.ResponseUsage;

public class AIHelperDialog extends HelperDialog {

	private static final long serialVersionUID = 1L;

	private static final String REQUEST_BUTTON_TEXT = "Perform";
	private static Map<String, ResponseInputItem> contextMap = new LinkedHashMap<>();

	private ILogger log;
	private JTextArea editor = new JTextArea();
	private JButton perform = new JButton(REQUEST_BUTTON_TEXT);

	public AIHelperDialog(final AICodeHelper codeHelper, final OpenAIClient client) throws JsonProcessingException {
		super(codeHelper);

		setAlwaysOnTop(true);
		setUndecorated(true);

		editor.setTabSize(2);
		editor.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		editor.setLineWrap(true);
		editor.setWrapStyleWord(true);

		JScrollPane comp = new JScrollPane(editor);
		comp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		comp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		comp.setPreferredSize(new Dimension(300, 150));

		String generalInfo;
		try {
			generalInfo = AEUtils.loadResource("/generalInfo.md");
			addContextInput("GeneralInfo", generalInfo);
		} catch (Exception e) {
			getLog().error("Resource: " + "/generalInfo.md" + " not found.", e);
		}

		JsonMapper mapper = new JsonMapper();

		VariableReport varInfo = getCodeHelper().appendSystemVariablesContext();
		String text = mapper.writeValueAsString(varInfo);
		addContextInput("SystemVariablesContext", text);

		List<Class<?>> processorClasses = ClassUtils.findAssignable(Processor.class);
		for (Class<?> processorClass : processorClasses) {
			CommandProcessorInfo info = codeHelper.getProcessorInfo(processorClass);
			text = mapper.writeValueAsString(info);
			addContextInput(processorClass.getName(), text);
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

	private void addContextInput(String name, String text) {
		Message message = com.openai.models.responses.ResponseInputItem.Message.builder()
				.role(com.openai.models.responses.ResponseInputItem.Message.Role.SYSTEM).addInputTextContent(text)
				.build();

		contextMap.put(name, ResponseInputItem.ofMessage(message));
	}

	protected void performRequest(OpenAIClient client) {
		try {
			TextEditor textEditor = getCodeHelper().getEditor();

			List<ResponseInputItem> inputs = new ArrayList<>();

			Collection<ResponseInputItem> values = contextMap.values();
			for (ResponseInputItem processorInfo : values) {
				String text = processorInfo.message().get().content().get(0).inputText().get().text();
				debug(new AELogRecord(text, "json", "Input"));
				inputs.add(processorInfo);
			}

			JsonMapper mapper = new JsonMapper();
			com.ganteater.ai.model.Editor editorInfo = new Editor();
			String text = textEditor.getText();
			editorInfo.setContent(text);

			int caretPosition = textEditor.getCaretPosition();
			int selectionStart = textEditor.getSelectionStart();
			int selectionEnd = textEditor.getSelectionEnd();
			editorInfo.setCaretPosition(caretPosition);

			Selection selection = new Selection();
			selection.setStartPosition(selectionStart);
			selection.setEndPosition(selectionEnd);
			editorInfo.setSelection(selection);
			try {
				String source = mapper.writeValueAsString(editorInfo);
				debug(new AELogRecord(source, "json", "Input"));

				Message message = com.openai.models.responses.ResponseInputItem.Message.builder()
						.role(com.openai.models.responses.ResponseInputItem.Message.Role.USER)
						.addInputTextContent(source).build();

				inputs.add(ResponseInputItem.ofMessage(message));

				String prompt = this.editor.getText();
				Message input = com.openai.models.responses.ResponseInputItem.Message.builder()
						.role(com.openai.models.responses.ResponseInputItem.Message.Role.USER)
						.addInputTextContent(prompt).build();

				inputs.add(ResponseInputItem.ofMessage(input));
				debug(new AELogRecord(prompt, "txt", "Input"));

				Builder builder = ResponseCreateParams.builder().model(getCodeHelper().getChatModel())
						.input(Input.ofResponse(inputs));

				Response response = client.responses().create(builder.build());
				logUsage(response.usage());

				response.output().forEach(item -> {
					if (item.isMessage()) {
						List<com.openai.models.responses.ResponseOutputMessage.Content> content = item.asMessage()
								.content();
						performMessage(content);
					}
				});
			} catch (JsonProcessingException e) {
				throw new IllegalArgumentException(e);
			}

		} catch (RateLimitException e) {
			OptionPane.showMessageDialog(getCodeHelper().getRecipePanel().getFrame(), e.getLocalizedMessage(),
					"Rate Limit", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void performMessage(List<com.openai.models.responses.ResponseOutputMessage.Content> content) {
		String responseText = content.get(0).outputText().get().text();
		debug(new AELogRecord(responseText, "xml", "Output"));

		getCodeHelper().hide();
		updateCode(responseText);
	}

	private void updateCode(String responseText) {
		JsonMapper mapper = new JsonMapper();
		try {
			JsonNode responseNode = mapper.readTree(responseText);

			TextEditor textEditor = getCodeHelper().getEditor();
			String code = responseNode.get("generatedOutputRecipeCode").asText();
			textEditor.setText(code);
			if (StringUtils.isNotBlank(code)) {
				textEditor.getRecipePanel().compileTask();

				TaskEditor recipePanel = getCodeHelper().getEditor().getRecipePanel();
				recipePanel.compileTask();
				recipePanel.refreshTaskTree();
				try {
					JsonNode jsonNode = responseNode.get("caretPosition");
					if (jsonNode != null) {
						int cursor = Integer.parseInt(jsonNode.asText());
						textEditor.setCaretPosition(cursor < 0 ? textEditor.getCaretPosition() : cursor);
					}
					JsonNode selection = responseNode.get("selection");
					if (selection != null) {
						int start = Integer.parseInt(selection.get("startPosition").asText());
						int end = Integer.parseInt(selection.get("endPosition").asText());
						textEditor.select(start, end);
					}
				} catch (IllegalArgumentException e1) {
					textEditor.setCaretPosition(code.length());
				}

			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void logUsage(Optional<ResponseUsage> optional) {
		if (optional.isPresent()) {
			ResponseUsage responseUsage = optional.get();
			long inputTokens = responseUsage.inputTokens();
			long inputCachedTokens = optional.get().inputTokensDetails().cachedTokens();
			long outputTokens = optional.get().outputTokens();
			long reasoningTokens = optional.get().outputTokensDetails().reasoningTokens();

			debug(String.format("Input: %1$d, cached: %2$d, output: %3$d, reasoning: %4$d tokens.", inputTokens,
					inputCachedTokens, outputTokens, reasoningTokens));
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
