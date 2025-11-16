package com.ganteater.ai;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

public enum Marker {
	CURSOR("Cursor position."), SELECTION_START("Start selection text position."),
	SELECTION_END("End selection text position.");

	private String description;

	Marker(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "[" + super.toString() + "]";
	}

	public int length() {
		return toString().length();
	}

	public String getDescription() {
		return description;
	}

	public int getPosition(String text) {
		return StringUtils.indexOf(text, toString());
	}

	public static MarkerExtractResult extractAll(String code) {
		Marker[] values = values();
		Map<Marker, Integer> posMap = new HashMap<Marker, Integer>();
		for (Marker marker : values) {
			int pos = code.indexOf(marker.toString());
			posMap.put(marker, pos);
		}

		LinkedHashMap<Marker, Integer> sortedMap = posMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
						LinkedHashMap::new));

		Map<Marker, Integer> result = new HashMap<Marker, Integer>();
		Set<Entry<Marker, Integer>> entrySet = sortedMap.entrySet();
		int correction = 0;
		for (Entry<Marker, Integer> entry : entrySet) {
			Integer value = entry.getValue();
			if (value > 0) {
				int pos = value - correction;
				Marker key = entry.getKey();
				correction = correction + key.toString().length();
				result.put(key, pos);
			}
		}

		String cleanedText = code;
		for (Marker marker : values) {
			cleanedText = cleanedText.replace(marker.toString(), "");
		}

		return new MarkerExtractResult(cleanedText, result);
	}
}
