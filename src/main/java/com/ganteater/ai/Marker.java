package com.ganteater.ai;

public enum Marker {
	CURSOR, SELECTION_START, SELECTION_END;

	@Override
	public String toString() {
		return "[" + super.toString() + "]";
	}

	int length() {
		return toString().length();
	}
}
