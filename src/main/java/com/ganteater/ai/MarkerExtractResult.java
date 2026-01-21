package com.ganteater.ai;

import java.util.Map;

public class MarkerExtractResult {

	String text;
	Map<Marker, Integer> posMap;

	MarkerExtractResult(String code, Map<Marker, Integer> posMap) {
		super();
		this.text = code;
		this.posMap = posMap;
	}

	public String getText() {
		return text;
	}

	public int getPosition(Marker marker) {
		Integer pos = posMap.get(marker);
		return pos == null ? -1 : pos;
	}

}
