package com.ganteater.ae.desktop.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.ganteater.ae.AELogRecord;
import com.ganteater.ae.desktop.ui.OptionPane;
import com.ganteater.ae.desktop.view.View;
import com.ganteater.ae.processor.Processor;
import com.ganteater.ae.util.ClassUtils;
import com.ganteater.ai.model.CommandProcessorInfo;
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
import com.openai.models.responses.ResponseOutputMessage.Content;
import com.openai.models.responses.ResponseUsage;

/**
 * Prompt dialog that builds request context, sends a completion request, and applies the returned editor updates.
 */
public class AIHelperDialog extends HelperDialog {

	private static final long serialVersionUID = 1L;

	private static final String OUTPUT_FORMAT_RESOURCE_NAME = "/output-format.md";
	private static final Map<String, ResponseInputItem> CONTEXT_MAP = new LinkedHashMap<>();

	private final JTextArea editor = new JTextArea();

	private Thread processThread;

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

		addContext("/general-info.md");
		addContext(OUTPUT_FORMAT_RESOURCE_NAME);

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
				if (cmp != AIHelperDialog.this && cmp != editor) {
					setVisible(false);
				}
			}
		});

	}

	private void addContext(String name) {
		try {
			URL systemResource = getClass().getResource(name);
			String generalInfo = IOUtils.toString(systemResource, "UTF8");
			addContextInput(name, generalInfo);
		} catch (Exception e) {
			getCodeHelper().getLog().error("Resource: " + name + " not found.", e);
		}
	}

	private synchronized void perform(final OpenAIClient client) {
		if (processThread == null) {
			processThread = new Thread(() -> {
				try {
					getCodeHelper().setProgress(true);
					setVisible(false);
					performRequest(client);
					processThread = null;

				} finally {
					getCodeHelper().setProgress(false);
				}
			});
			processThread.start();
		}
	}

	private void addContextInput(String name, String processorInfo) {
		Message message = com.openai.models.responses.ResponseInputItem.Message.builder()
				.role(com.openai.models.responses.ResponseInputItem.Message.Role.SYSTEM)
				.addInputTextContent(processorInfo).build();

		CONTEXT_MAP.put(name, ResponseInputItem.ofMessage(message));
	}

	protected void performRequest(OpenAIClient client) {
		try {
			TextEditor textEditor = getCodeHelper().getEditor();

			List<ResponseInputItem> inputs = new ArrayList<>();

			Collection<Entry<String, ResponseInputItem>> contextEntrySet = CONTEXT_MAP.entrySet();
			for (Entry<String, ResponseInputItem> contextEntry : contextEntrySet) {
				ResponseInputItem value = contextEntry.getValue();
				String text = value.message().get().content().get(0).inputText().get().text();
				if (!StringUtils.contains(contextEntry.getKey(), OUTPUT_FORMAT_RESOURCE_NAME)) {
					debug(new AELogRecord(text, "txt", null));
				}
				inputs.add(value);
			}

			JsonMapper mapper = new JsonMapper();
			com.ganteater.ai.model.Editor editorInfo = new com.ganteater.ai.model.Editor();
			String text = textEditor.getText();
			editorInfo.setContent(text);

			int caretPosition = textEditor.getCaretPosition();
			int selectionStart = textEditor.getSelectionStart();
			int selectionEnd = textEditor.getSelectionEnd();
			editorInfo.setCaretPosition(caretPosition);

			com.ganteater.ai.model.Selection selection = new com.ganteater.ai.model.Selection();
			selection.setStartPosition(selectionStart);
			selection.setEndPosition(selectionEnd);
			editorInfo.setSelection(selection);
			try {
				String source = mapper.writeValueAsString(editorInfo);
				debug(new AELogRecord(source, "json", null));

				Message message = com.openai.models.responses.ResponseInputItem.Message.builder()
						.role(com.openai.models.responses.ResponseInputItem.Message.Role.USER)
						.addInputTextContent(source).build();

				inputs.add(ResponseInputItem.ofMessage(message));

				String prompt = this.editor.getText();
				Message input = com.openai.models.responses.ResponseInputItem.Message.builder()
						.role(com.openai.models.responses.ResponseInputItem.Message.Role.USER)
						.addInputTextContent(prompt).build();

				inputs.add(ResponseInputItem.ofMessage(input));
				getCodeHelper().getLog().info(new AELogRecord(prompt, "txt", null));

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

	private void performMessage(List<Content> content) {
		if (processThread == Thread.currentThread()) {
			String responseText = content.get(0).outputText().get().text();
			debug(new AELogRecord(responseText, "xml", "Output"));

			getCodeHelper().hide();
			updateCode(responseText);
		}
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

			getCodeHelper().getLog()
					.debug(String.format("Input: %1$d, cached: %2$d, output: %3$d, reasoning: %4$d tokens.",
							inputTokens, inputCachedTokens, outputTokens, reasoningTokens));
		}
	}

	private void debug(Object message) {
		if (getCodeHelper().isDebug()) {
			getCodeHelper().getLog().debug(message);
		}
	}

	/**
	 * Parses a JSON string into an OpenAI {@link JsonValue}.
	 *
	 * <p>
	 * This method does not attempt to normalize quotes; callers must provide valid JSON.
	 * </p>
	 *
	 * @param value JSON text
	 * @return parsed JSON
	 */
	public static JsonValue jsonValue(String value) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			JsonNode node = objectMapper.readTree(value);
			return JsonValue.fromJsonNode(node);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public Object getProcessorDescription(Object processorName) {
		return null;
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

	public void cancelRequest() {
		processThread = null;
	}

}
