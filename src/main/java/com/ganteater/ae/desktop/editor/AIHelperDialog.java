package com.ganteater.ae.desktop.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import com.ganteater.ae.processor.Processor;
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
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseUsage;
import com.openai.models.responses.Tool;
import com.openai.services.blocking.ResponseService;

public class AIHelperDialog extends HelperDialog {

	private static final String GET_PROCESSOR_INFO = "getProcessorHelp";

	private JTextArea editor = new JTextArea();
	private String generalInfo;
	private double temperature = 0.7;

	private JButton perform = new JButton("Perform");

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
				TextEditor text = getCodeHelper().getEditor();
				Set<String> processorClassList = getProcessorNames(text);
				new Thread(() -> performRequest(client, processorClassList)).start();
				setVisible(false);
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
				if (e.getOppositeComponent() != AIHelperDialog.this && e.getOppositeComponent() != editor) {
					setVisible(false);
				}
			}
		});

	}

	protected void performRequest(OpenAIClient client, Set<String> processors) {
		try {
			perform.setEnabled(false);
			Response response = request(client, processors);
			List<ResponseOutputItem> output = response.output();
			ResponseOutputItem responseOutputItem = output.get(0);

			Optional<ResponseFunctionToolCall> functionCall = responseOutputItem.functionCall();
			if (functionCall.isPresent()) {
				ResponseFunctionToolCall responseFunctionToolCall = functionCall.get();
				String name = responseFunctionToolCall.name();

				String arguments = responseFunctionToolCall.arguments();

				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode argumentsJson;
				argumentsJson = objectMapper.readTree(arguments);

				getLog().debug("Call Function Tool: " + name + ", arguments: " + argumentsJson);
				String processorName = argumentsJson.get("processorName").textValue();

				String fullProcessorName = Processor.getFullClassName(processorName);

				Set<String> processorClassNameList = new HashSet<>();
				processorClassNameList.add(fullProcessorName);
				processorClassNameList.add(BaseProcessor.class.getSimpleName());

				response = request(client, processorClassNameList);
				output = response.output();
				responseOutputItem = output.get(0);
			}

			Optional<ResponseOutputMessage> messageOpt = responseOutputItem.message();
			if (messageOpt.isPresent()) {
				ResponseOutputMessage message = messageOpt.get();
				String responseText = message.content().get(0).outputText().get().text();
				getLog().info(new AELogRecord(responseText, "xml", "Response"));

				getCodeHelper().hide();

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

		} catch (RateLimitException e) {
			OptionPane.showMessageDialog(getCodeHelper().getRecipePanel().getFrame(), e.getLocalizedMessage(),
					"Rate Limit", JOptionPane.ERROR_MESSAGE);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		} finally {
			perform.setEnabled(true);
		}
	}

	private Response request(OpenAIClient client, Set<String> processorClassList) {
		TextEditor textEditor = getCodeHelper().getEditor();

		int caretPosition = textEditor.getCaretPosition();
		int selectionStart = textEditor.getSelectionStart();
		int selectionEnd = textEditor.getSelectionEnd();
		String text = textEditor.getText();

		Prompt.Builder promptBuilder = new Prompt.Builder();
		promptBuilder.context(generalInfo);

		AIHelper aiHelper = getCodeHelper();
		aiHelper.appendExampleContext(promptBuilder, processorClassList);
		aiHelper.appendSystemVariablesContext(promptBuilder);

		Builder builder = ResponseCreateParams.builder();
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

		promptBuilder.source(text, "xml", caretPosition, selectionStart, selectionEnd)
				.input(editor.getText());

		String input = promptBuilder.build().buildPrompt();
		getLog().info(new AELogRecord(input, "md", "Input"));

		String chatModel = getCodeHelper().getChatModel();
		ResponseCreateParams params = builder
				.temperature(temperature)
				.addTool(function)
				.model(chatModel)
				.input(input)
				.build();

		ResponseService responses = client.responses();
		Response response = responses.create(params);

		ResponseUsage responseUsage = response.usage().get();
		long inputTokens = responseUsage.inputTokens();
		long inputCachedTokens = responseUsage.inputTokensDetails().cachedTokens();
		long outputTokens = responseUsage.outputTokens();
		long reasoningTokens = responseUsage.outputTokensDetails().reasoningTokens();

		getLog().debug(
				String.format("Input: %1$d, cached: %2$d, output: %3$d, reasoning: %4$d tokens.",
						inputTokens, inputCachedTokens, outputTokens, reasoningTokens));

		return response;
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

	public Set<String> getProcessorNames(TextEditor textEditor) {
		Set<String> processorClassList = new HashSet<>();
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
