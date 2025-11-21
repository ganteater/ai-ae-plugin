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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ganteater.ae.AELogRecord;
import com.ganteater.ae.ILogger;
import com.ganteater.ae.desktop.ui.OptionPane;
import com.ganteater.ae.processor.BaseProcessor;
import com.ganteater.ae.util.xml.easyparser.EasyParser;
import com.ganteater.ae.util.xml.easyparser.Node;
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
import com.openai.models.responses.ResponseInputItem.Message;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseOutputMessage.Content;
import com.openai.models.responses.ResponseUsage;

public class AIHelperDialog extends HelperDialog {

	private static final String REQUEST_BUTTON_TEXT = "Perform";

	private JTextArea editor = new JTextArea();
	private String generalInfo;

	private JButton perform = new JButton(REQUEST_BUTTON_TEXT);

	private ILogger log;

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
		comp.setPreferredSize(new Dimension(300, 150));

		this.generalInfo = loadResource("/generalInfo.md");

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

	protected void performRequest(OpenAIClient client) {
		try {
			String input = getInput();
			debug(new AELogRecord(input, "md", "Input"));

			List<ResponseInputItem> inputs = new ArrayList<>();

			Message message = com.openai.models.responses.ResponseInputItem.Message
					.builder()
					.role(com.openai.models.responses.ResponseInputItem.Message.Role.USER)
					.addInputTextContent(input)
					.build();

			inputs.add(ResponseInputItem.ofMessage(message));

			Builder builder = ResponseCreateParams.builder()
					.model(getCodeHelper().getChatModel())
					.addTool(GetProcessorInfo.class)
					.input(Input.ofResponse(inputs));

			Response response = client.responses().create(builder.build());
			logUsage(response.usage());

			response.output().forEach(item -> {
				if (item.isFunctionCall()) {
					ResponseFunctionToolCall functionCall = item.asFunctionCall();

					inputs.add(ResponseInputItem.ofFunctionCall(functionCall));
					inputs.add(ResponseInputItem.ofFunctionCallOutput(ResponseInputItem.FunctionCallOutput.builder()
							.callId(functionCall.callId())
							.outputAsJson(callFunction(functionCall, this))
							.build()));
				}
				if (item.isReasoning()) {
					ResponseInputItem reasoning = ResponseInputItem.ofReasoning(item.asReasoning());
					inputs.add(reasoning);
				}
			});

			if (!inputs.isEmpty()) {
				builder.input(ResponseCreateParams.Input.ofResponse(inputs));
				response = client.responses().create(builder.build());
				logUsage(response.usage());
			}

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

	private static Object callFunction(ResponseFunctionToolCall function, AIHelperDialog helperDialog) {
		switch (function.name()) {
		case "GetProcessorInfo":
			GetProcessorInfo getProcessorInfo = function.arguments(GetProcessorInfo.class);
			getProcessorInfo.helperDialog = helperDialog;
			return getProcessorInfo.execute();
		default:
			throw new IllegalArgumentException("Unknown function: " + function.name());
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

	private String getInput() {
		TextEditor textEditor = getCodeHelper().getEditor();

		int caretPosition = textEditor.getCaretPosition();
		int selectionStart = textEditor.getSelectionStart();
		int selectionEnd = textEditor.getSelectionEnd();
		String text = textEditor.getText();

		Prompt.Builder promptBuilder = new Prompt.Builder();
		promptBuilder.context(generalInfo);
		promptBuilder.source(text, "xml", caretPosition, selectionStart, selectionEnd)
				.input(editor.getText());

		AICodeHelper aiHelper = getCodeHelper();
		Collection<String> processors = getProcessorNames(textEditor);
		aiHelper.appendExampleContext(promptBuilder, processors);
		aiHelper.appendSystemVariablesContext(promptBuilder);

		return promptBuilder.build().buildPrompt();
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

	public Collection<String> getProcessorNames(TextEditor textEditor) {
		List<String> processorClassList = new ArrayList<>();
		processorClassList.add(BaseProcessor.class.getSimpleName());
		try {
			Node taskNode = new EasyParser().getObject(textEditor.getRecipePanel().getEditor().getText());
			if (taskNode != null) {
				Node[] nodes = taskNode.getNodes("Extern");
				for (Node node : nodes) {
					String processorClassName = node.getAttribute("class");
					processorClassList.add(processorClassName);
				}
			}
		} catch (Exception e) {
			getLog().error("Recipe parsing failed.", e);
		}

		return processorClassList;
	}

	public Object getProcessorDescription(Object processorName) {
		return null;
	}

	public String loadResource(String name) {
		String result = null;
		try (InputStream is = AIHelperDialog.class.getResourceAsStream(name)) {
			result = IOUtils.toString(is);
		} catch (IOException e) {
			getLog().error("Resource: " + name + " not found.", e);
		}
		return result;
	}

	private ILogger getLog() {
		log = getCodeHelper().getEditor().getRecipePanel().getLogger();
		if (log == null) {
			log = getCodeHelper().getRecipePanel().createLog("Helper", true);
		}
		return log;
	}

	private static final long serialVersionUID = 1L;

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

	@JsonClassDescription("Get help documentation about anteater command Processor.")
	static class GetProcessorInfo {
		@JsonPropertyDescription("The name of command processor.")
		public String name;

		protected AIHelperDialog helperDialog;

		public String execute() {
			List<String> processorClassNames = new ArrayList<>();
			processorClassNames.add(name);

			com.ganteater.ai.Prompt.Builder promptBuilder = new Prompt.Builder();
			helperDialog.getCodeHelper().appendExampleContext(promptBuilder, processorClassNames);
			String text = promptBuilder.build().buildPrompt();
			helperDialog.debug(new AELogRecord(text, "md", "GetProcessorInfo:" + name));
			return text;
		}
	}

}
