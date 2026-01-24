package com.ganteater.ae.desktop.editor;

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

public class AICodeHelper extends CodeHelper {

	private String chatModel;
	private boolean debug;

	private JLabel aiProgress = new JLabel(AEFrame.getIcon("spinner.gif"));

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
		chatModel = StringUtils.defaultIfEmpty(model, "gpt-5-mini");
		debug = Boolean.parseBoolean(taskProcessor.attr(editorNode, "debug", "false"));

		new Thread(() -> {
			OpenAIClient client;
			try {
				setProgress(true);
				client = createClient(taskProcessor, editorNode);
				AIHelperDialog aiHelperDialog = new AIHelperDialog(this, client);
				super.setDefaultDialog(aiHelperDialog);
				setProgress(false);
			} catch (IOException e) {
				getLog().error("Critical error: AI Helper could not be initialized.", e);
			}
		}).start();
	}

	protected OpenAIClient createClient(Processor taskProcessor, Node editorNode) throws IOException {
		String apiKey = taskProcessor.attr(editorNode, "apiKey");
		if (apiKey == null) {
			throw new IllegalArgumentException("apiKey attribute required.");
		}

		String baseUrl = taskProcessor.attr(editorNode, "baseUrl");
		OpenAIClient client = OpenAIOkHttpClient.builder().apiKey(apiKey).baseUrl(baseUrl).build();
		return client;
	}

	public String appendViewInfo(Class<?> clazz) {
		StringBuilder contextBuilder = new StringBuilder();

		contextBuilder.append("# View: " + clazz + "\n\n");

		CommandDescription description = clazz.getAnnotation(CommandDescription.class);
		if (description != null) {
			contextBuilder.append(description.value() + "\n\n");
		}

		return contextBuilder.toString();
	}

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

	public String getChatModel() {
		return chatModel;
	}

	public boolean isDebug() {
		return debug;
	}

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

	public void setProgress(boolean process) {
		aiProgress.setVisible(process);
	}

	public ILogger getLog() {
		ILogger log = getEditor().getRecipePanel().getLogger();
		if (log == null) {
			log = getRecipePanel().createLog("Helper", true);
		}
		return log;
	}

}
