package com.ganteater.ae.desktop.editor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.apache.commons.lang.StringUtils;

import com.ganteater.ae.ILogger;
import com.ganteater.ae.desktop.ui.AEFrame;
import com.ganteater.ae.desktop.ui.TaskPanel;
import com.ganteater.ae.processor.CommandInfo;
import com.ganteater.ae.processor.Processor;
import com.ganteater.ae.processor.annotation.CommandDescription;
import com.ganteater.ae.util.xml.easyparser.Node;
import com.ganteater.ai.model.CommandProcessorInfo;
import com.ganteater.ai.model.VariableReport;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

/**
 * Installs and configures the AI helper integration for a {@link TextEditor}.
 *
 * <p>
 * This helper adds a small progress indicator to the editor UI, reads model/client configuration from the editor
 * node, initializes an {@link OpenAIClient} in a background thread, and wires an {@link AIHelperDialog} to drive
 * request/cancel behavior.
 * </p>
 */
public class AICodeHelper extends CodeHelper {

	private static final String DEFAULT_MODEL = "gpt-5-mini";

	private String chatModel;
	private boolean debug;

	private final JLabel aiProgress = new JLabel(AEFrame.getIcon("spinner.gif"));

	/**
	 * Creates the helper for a specific editor instance.
	 *
	 * <p>
	 * The OpenAI client is created asynchronously; initialization failures are logged and the helper remains disabled.
	 * </p>
	 *
	 * @param textEditor editor to attach to
	 * @throws IOException if configuration cannot be read or the client cannot be initialized
	 * @throws IllegalAccessException if reflective access used by the base helper fails
	 */
	public AICodeHelper(TextEditor textEditor) throws IOException, IllegalAccessException {
		super(textEditor);

		TaskEditor recipePanel = textEditor.getRecipePanel();
		TaskPanel taskPanel = recipePanel.getMainPanel();

		aiProgress.setBorder(BorderFactory.createEmptyBorder());
		aiProgress.setOpaque(false);
		taskPanel.addButton(aiProgress);

		TaskEditor recipeEditor = getRecipePanel();
		Processor taskProcessor = recipeEditor.getProcessor();

		Node editorNode = recipeEditor.getEditor().getEditorNode();

		String model = taskProcessor.attr(editorNode, "model");
		chatModel = StringUtils.defaultIfEmpty(model, DEFAULT_MODEL);
		debug = Boolean.parseBoolean(taskProcessor.attr(editorNode, "debug", "false"));

		new Thread(() -> {
			try {
				setProgress(true);
				OpenAIClient client = createClient(taskProcessor, editorNode);
				AIHelperDialog aiHelperDialog = new AIHelperDialog(this, client);
				aiProgress.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						aiHelperDialog.cancelRequest();
						setProgress(false);
					}
				});

				super.setDefaultDialog(aiHelperDialog);
			} catch (IOException e) {
				getLog().error("Critical error: AI Helper could not be initialized.", e);
			} finally {
				setProgress(false);
			}
		}).start();

	}

	/**
	 * Creates an OpenAI-compatible client using configuration on the editor node.
	 *
	 * <p>
	 * Expected attributes:
	 * </p>
	 * <ul>
	 *   <li>{@code apiKey} (required)</li>
	 *   <li>{@code baseUrl} (optional; depends on the backend)</li>
	 * </ul>
	 *
	 * @param taskProcessor processor used to read attributes
	 * @param editorNode editor configuration node
	 * @return initialized client
	 * @throws IOException if client creation fails
	 */
	protected OpenAIClient createClient(Processor taskProcessor, Node editorNode) throws IOException {
		String apiKey = taskProcessor.attr(editorNode, "apiKey");
		if (apiKey == null) {
			throw new IllegalArgumentException("apiKey attribute required.");
		}

		String baseUrl = taskProcessor.attr(editorNode, "baseUrl");
		return OpenAIOkHttpClient.builder().apiKey(apiKey).baseUrl(baseUrl).build();
	}

	/**
	 * Builds a textual description of a {@link com.ganteater.ae.desktop.view.View} class for request context.
	 *
	 * @param clazz view class
	 * @return formatted markdown-like text
	 */
	public String appendViewInfo(Class<?> clazz) {
		StringBuilder contextBuilder = new StringBuilder();

		contextBuilder.append("# View: " + clazz + "\n\n");

		CommandDescription description = clazz.getAnnotation(CommandDescription.class);
		if (description != null) {
			contextBuilder.append(description.value() + "\n\n");
		}

		return contextBuilder.toString();
	}

	/**
	 * Collects available system variable names for request context.
	 *
	 * @return report containing the variable scope and variable names
	 */
	public VariableReport appendSystemVariablesContext() {
		Map<String, Object> startVariables = getRecipePanel().getManager().getSystemVariables();

		VariableReport result = new VariableReport();
		result.setScope("SystemVariables");

		Set<String> keySet = startVariables.keySet();
		List<String> arrayList = new ArrayList<>();
		for (String name : keySet) {
			arrayList.add(name);
		}
		result.setVariables(arrayList);
		return result;
	}

	/**
	 * @return model name used for the request
	 */
	public String getChatModel() {
		return chatModel;
	}

	/**
	 * @return whether debug logging is enabled
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * Builds a structured description of a processor and its commands for request context.
	 *
	 * @param processorClass processor implementation class
	 * @return processor metadata including command names, descriptions, and examples
	 */
	public CommandProcessorInfo getProcessorInfo(Class<?> processorClass) {
		CommandProcessorInfo result = new CommandProcessorInfo();

		result.setClass_(processorClass.getName());
		CommandDescription descriptionAnnotation = processorClass.getAnnotation(CommandDescription.class);
		if (descriptionAnnotation != null) {
			result.setDescription(descriptionAnnotation.value());
		}

		List<CommandInfo> commandList = super.getCommandList(null, processorClass);
		List<com.ganteater.ai.model.CommandInfo> arrayList = commandList.stream().map(item -> {
			com.ganteater.ai.model.CommandInfo commandInfo = new com.ganteater.ai.model.CommandInfo();
			commandInfo.setCommandName(item.getName());
			commandInfo.setDescription(item.getDescription());
			commandInfo.setUsecases(item.getExamples());
			return commandInfo;
		}).collect(Collectors.toList());
		result.setCommands(arrayList);

		return result;
	}

	/**
	 * Shows or hides the progress indicator.
	 *
	 * @param process {@code true} to show, {@code false} to hide
	 */
	public void setProgress(boolean process) {
		aiProgress.setVisible(process);
	}

	/**
	 * Returns the logger used by the editor, creating one if needed.
	 *
	 * @return logger instance
	 */
	public ILogger getLog() {
		ILogger log = getEditor().getRecipePanel().getLogger();
		if (log == null) {
			log = getRecipePanel().createLog("Helper", true);
		}
		return log;
	}

}
