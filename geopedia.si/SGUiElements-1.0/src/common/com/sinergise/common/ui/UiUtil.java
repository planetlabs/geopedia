package com.sinergise.common.ui;

public class UiUtil {
	public static String ensureColonForLabel(String label) {
		label = label.trim();
		if (label.endsWith(":")) {
			return label;
		}
		return label+":";
	}
}
