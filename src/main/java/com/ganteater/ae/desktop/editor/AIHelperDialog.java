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
import com.openai.core.JsonString;
import com.openai.core.JsonValue;
import com.openai.errors.RateLimitException;
import com.openai.models.responses.FunctionTool;
import com.openai.models.responses.FunctionTool.Parameters;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseCreateParams.Builder;
import com.openai.models.responses.ResponseCreateParams.Input;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseInputItem.FunctionCallOutput;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseUsage;
import com.openai.models.responses.Tool;
import com.openai.services.blocking.ResponseService;

public class AIHelperDialog extends HelperDialog {

	private static final String REQUEST_BUTTON_TEXT = "Perform";

	private static final String GET_PROCESSOR_INFO = "getProcessorHelp";

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
		});

		editor.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					setVisible(false);
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

	protected void performRequest(OpenAIClient client) {
		try {
			String input = getInput();
			debug(new AELogRecord(input, "md", "Input"));

			Builder builder = ResponseCreateParams.builder();

			List<ResponseInputItem> inputs = getMessages(input);

			Tool tool = getTool();
			ResponseCreateParams params = builder.model(getCodeHelper().getChatModel())
					.addTool(tool)
					.input(Input.ofResponse(inputs))
					.build();

			Response response = client.responses().create(params);
			logUsage(response.usage());

			List<ResponseOutputItem> output = response.output();

			for (ResponseOutputItem responseOutputItem : output) {
				Optional<ResponseFunctionToolCall> functionCall = responseOutputItem.functionCall();
				if (functionCall.isPresent()) {
					ResponseFunctionToolCall responseFunctionToolCall = functionCall.get();
					String name = responseFunctionToolCall.name();
					String id = responseFunctionToolCall.id().get();
					String callId = responseFunctionToolCall.callId();

					String arguments = responseFunctionToolCall.arguments();

					ObjectMapper objectMapper = new ObjectMapper();
					JsonNode argumentsJson;
					argumentsJson = objectMapper.readTree(arguments);

					debug("Call Function Tool: " + name + ", arguments: " + argumentsJson);
					String processorName = argumentsJson.get("processorName").textValue();

					List<String> processorClassNames = new ArrayList<>();
					processorClassNames.add(processorName);

					com.ganteater.ai.Prompt.Builder promptBuilder = new Prompt.Builder();
					getCodeHelper().appendExampleContext(promptBuilder, processorClassNames);
					String processorInfo = promptBuilder.build().buildPrompt();

					getLog().debug("callId: " + callId);

					ResponseService responses = client.responses();

					FunctionCallOutput callOutput = FunctionCallOutput.builder()
							.id(id)
							.callId(callId)
							.output(processorInfo)
							.status(ResponseInputItem.FunctionCallOutput.Status.COMPLETED)
							.build();

//					Message callOutput = com.openai.models.responses.ResponseInputItem.Message
//							.builder()
//							.role(com.openai.models.responses.ResponseInputItem.Message.Role.USER)
//							.addInputTextContent(processorInfo).build();
//					inputs.add(ResponseInputItem.ofMessage(callOutput));

					inputs.add(ResponseInputItem.ofFunctionCallOutput(callOutput));
					
					params = ResponseCreateParams.builder()
							.model(getCodeHelper().getChatModel())
							.addTool(tool)
							.input(Input.ofResponse(inputs))
							.build();
					response = responses.create(params);

					output = response.output();
					break;
				}

				Optional<ResponseOutputMessage> messageOpt = getMessage(output);
				if (messageOpt.isPresent()) {
					ResponseOutputMessage resultMessage = messageOpt.get();
					String responseText = resultMessage.content().get(0).outputText().get().text();
					debug(new AELogRecord(responseText, "xml", "Output"));

					getCodeHelper().hide();

					updateCode(responseText);
					break;
				}
			}

		} catch (RateLimitException e) {
			OptionPane.showMessageDialog(getCodeHelper().getRecipePanel().getFrame(), e.getLocalizedMessage(),
					"Rate Limit", JOptionPane.ERROR_MESSAGE);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
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

		AIHelper aiHelper = getCodeHelper();
		Collection<String> processors = getProcessorNames(textEditor);
		aiHelper.appendExampleContext(promptBuilder, processors);
		aiHelper.appendSystemVariablesContext(promptBuilder);

		return promptBuilder.build().buildPrompt();
	}

	private List<ResponseInputItem> getMessages(String input) {
		List<ResponseInputItem> inputs = new ArrayList<>();

		com.openai.models.responses.ResponseInputItem.Message message = com.openai.models.responses.ResponseInputItem.Message
				.builder()
				.role(com.openai.models.responses.ResponseInputItem.Message.Role.USER)
				.addInputTextContent(input).build();
		inputs.add(ResponseInputItem.ofMessage(message));
		return inputs;
	}

	private Tool getTool() {
		com.openai.models.responses.FunctionTool.Builder ft_builder = FunctionTool.builder();

		ft_builder.name(GET_PROCESSOR_INFO).description("Get help documentation about anteater command Processor.");

		Parameters ft_params = Parameters.builder()
				.putAdditionalProperty("properties", jsonValue("{'processorName': {'type': 'string'}}"))
				.putAdditionalProperty("type", JsonString.of("object"))
				.putAdditionalProperty("required", jsonValue("['processorName']"))
				.build();

		ft_builder.parameters(ft_params);

		FunctionTool functionTool = ft_builder.strict(false).build();
		Tool function = Tool.ofFunction(functionTool);
		return function;
	}

	private Optional<ResponseOutputMessage> getMessage(List<ResponseOutputItem> output) {
		return output.get(output.size() - 1).message();
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
		Node taskNode = new EasyParser().getObject(textEditor.getRecipePanel().getEditor().getText());
		if (taskNode != null) {
			Node[] nodes = taskNode.getNodes("Extern");
			for (Node node : nodes) {
				String processorClassName = node.getAttribute("class");
				processorClassList.add(processorClassName);
			}
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
	public AIHelper getCodeHelper() {
		return (AIHelper) super.getCodeHelper();
	}

}
