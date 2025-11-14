package com.ganteater.ai;

import java.util.function.Consumer;

public class Prompt {
	private final String context;
	private final String instruction;
	private final String examples;
	private final String input;
	private String hint;
	private String source;

	// Private constructor to enforce usage of the Builder
	private Prompt(Builder builder) {
		this.context = builder.context;
		this.instruction = builder.instruction;
		this.examples = builder.examples;
		this.input = builder.input;
		this.hint = builder.hint;
		this.source = builder.source;
	}

	// Getters
	public String getContext() {
		return context;
	}

	public String getInstruction() {
		return instruction;
	}

	public String getExamples() {
		return examples;
	}

	public String getInput() {
		return input;
	}

	public String getHint() {
		return hint;
	}

	public String getSource() {
		return source;
	}

	// Method to build the final prompt string
	public String buildPrompt() {
		StringBuilder promptBuilder = new StringBuilder();

		if (context != null && !context.isEmpty()) {
			promptBuilder.append("Context:\n").append(context).append("\n\n");
		}
		if (source != null && !source.isEmpty()) {
			promptBuilder.append("Source:\n").append(source).append("\n\n");
		}
		if (instruction != null && !instruction.isEmpty()) {
			promptBuilder.append("Instruction:\n").append(instruction).append("\n\n");
		}
		if (hint != null && !hint.isEmpty()) {
			promptBuilder.append("Hint:\n").append(hint).append("\n\n");
		}
		if (examples != null && !examples.isEmpty()) {
			promptBuilder.append("Examples:\n").append(examples).append("\n\n");
		}
		if (input != null && !input.isEmpty()) {
			promptBuilder.append("Input:\n").append(input).append("\n");
		}

		return promptBuilder.toString();
	}

	// Builder Class
	public static class Builder {
		private String context;
		private String instruction = markerInstraction();
		private String examples;
		private String input;
		private String hint;
		private String source;

		public Builder() {
		}

		private String markerInstraction() {
			StringBuilder markerInstraction = new StringBuilder(
					"In the source can be used following special markers:\n");
			Marker[] values = Marker.values();
			for (int i = 0; i < values.length; i++) {
				markerInstraction.append(i + ". " + values[i].toString() + " - " + values[i].getDescription() + "\n");
			}

			return markerInstraction.toString();
		}

		public Builder setContext(String context) {
			this.context = context;
			return this;
		}

		public Builder setInstruction(String instruction) {
			this.instruction = instruction;
			return this;
		}

		public Builder setExamples(String examples) {
			this.examples = examples;
			return this;
		}

		public Builder setInput(String input) {
			this.input = input;
			return this;
		}

		public Builder setHint(String hint) {
			this.hint = hint;
			return this;
		}

		public Builder setSource(String source, int caretPosition, int selectionStart, int selectionEnd) {
			StringBuilder textWithCursor = new StringBuilder(source);
			if (selectionStart == selectionEnd) {
				selectionStart = -1;
			}

			if (selectionStart > 0) {
				textWithCursor.insert(selectionStart, Marker.SELECTION_START);
				textWithCursor.insert(selectionEnd + Marker.SELECTION_START.length(), Marker.SELECTION_END);
			} else {
				textWithCursor.insert(caretPosition, Marker.CURSOR);
			}

			this.source = textWithCursor.toString();
			return this;
		}

		public Prompt build() {
			return new Prompt(this);
		}
	}

	public void apply(Consumer<String> consumer) {
		String promptString = buildPrompt();
		consumer.accept(promptString);
	}
}