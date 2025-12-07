package com.ganteater.ae.desktop.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.ganteater.ae.processor.Processor;
import com.ganteater.ae.processor.annotation.CommandDescription;
import com.ganteater.ae.util.xml.easyparser.Node;
import com.ganteater.ai.model.CommandInfo;
import com.ganteater.ai.model.CommandProcessorInfo;
import com.ganteater.ai.model.VariableReport;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

public class AICodeHelper extends CodeHelper {

	private boolean debug;
	private String chatModel;

	public AICodeHelper(TextEditor textEditor) throws IOException, IllegalAccessException {
		super(textEditor);

		TaskEditor recipeEditor = getRecipePanel();
		Processor taskProcessor = recipeEditor.getTaskProcessor();

		Node editorNode = recipeEditor.getEditor().getEditorNode();

		chatModel = StringUtils.defaultIfEmpty(taskProcessor.attr(editorNode, "model"), "gpt-5-mini");
		debug = Boolean.parseBoolean(taskProcessor.attr(editorNode, "debug", "false"));

		String apiKey = taskProcessor.attr(editorNode, "apiKey");
		if (apiKey == null) {
			throw new IllegalArgumentException("apiKey attribute required.");
		}

		OpenAIClient client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();

		AIHelperDialog aiHelperDialog = new AIHelperDialog(this, client);
		super.setDefaultDialog(aiHelperDialog);
	}

	public CommandProcessorInfo getProcessorInfo(Class<?> clazz) {
		CommandProcessorInfo info = new CommandProcessorInfo();

		info.setName("Command Processor: `" + clazz.getSimpleName() + "`");
		info.setClass_(clazz.getName());

		CommandDescription descriptionAnnotation = clazz.getAnnotation(CommandDescription.class);
		if (descriptionAnnotation != null) {
			ArrayList<Object> sections = new ArrayList<Object>();
			sections.add(descriptionAnnotation.value());
		}

		List<com.ganteater.ae.processor.CommandInfo> commandList = super.getCommandList(null, clazz);
		List<CommandInfo> commandInfoList = new ArrayList<CommandInfo>();
		for (com.ganteater.ae.processor.CommandInfo cominfo : commandList) {
			CommandInfo commandInfo = new CommandInfo();

			if (!cominfo.getName().equals("init")) {
				commandInfo.setCommandName(cominfo.getName());
			} else {
				commandInfo.setCommandName("Processor Initialization");
			}

			String description = cominfo.getDescription();
			commandInfo.setDescription(description);

			commandInfo.setUsecases(fillExampes(cominfo));
			commandInfoList.add(commandInfo);
		}

		info.setCommands(commandInfoList);
		return info;
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

	private List<String> fillExampes(com.ganteater.ae.processor.CommandInfo cominfo) {
		List<String> sections = new ArrayList<>();
		List<String> examples = cominfo.getExamples();
		if (!examples.isEmpty()) {
			for (String example : examples) {
				sections.add(appendExample(example));
			}
		}
		return sections;
	}

	private String appendExample(String example) {
		String result = null;

		String code = example;
		String description = "";

		int colonIndex = StringUtils.indexOf(example, ':');
		int startTagIndex = StringUtils.indexOf(example, '<');
		if (colonIndex >= 0 && colonIndex < startTagIndex) {
			code = StringUtils.substring(example, colonIndex + 1);
			description = StringUtils.substring(example, 0, colonIndex + 1);
		}

		code = code.replace("'", "\"");
		if (StringUtils.contains(example, "\n")) {
			result = description + "\n" + "```xml\n" + code + "\n```";
		} else {
			result = description + "`" + code + "`";
		}

		return result;
	}

	public VariableReport appendSystemVariablesContext() {
		VariableReport variableReport = new VariableReport();

		Map<String, Object> startVariables = getRecipePanel().getManager().getSystemVariables();
		variableReport.setScope("System Variable Names");

		ArrayList<String> arrayList = new ArrayList<>();

		Set<String> keySet = startVariables.keySet();
		for (String name : keySet) {
			arrayList.add(name);
		}
		variableReport.setVariables(arrayList);
		return variableReport;
	}

	public String getChatModel() {
		return chatModel;
	}

	public boolean isDebug() {
		return debug;
	}

}
