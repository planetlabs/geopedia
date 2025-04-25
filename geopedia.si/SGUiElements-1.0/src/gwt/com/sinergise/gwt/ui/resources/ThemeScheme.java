package com.sinergise.gwt.ui.resources;

public class ThemeScheme {
	public String mainColor = "#0092ed";
	public String secondColor = "#dddddd";
	public String textColor = "#555555";
	public String headingColor = "#222222";

	public static String getMainColor() {
		return Theme.getColorScheme().mainColor;
	}
	public static String getSecondColor() {
		return Theme.getColorScheme().secondColor;
	}
	public static String getTextColor() {
		return Theme.getColorScheme().textColor;
	}
	public static String getHeadingColor() {
		return Theme.getColorScheme().headingColor;
	}

	@Deprecated
	public static void setMainColor(String mainColor) {
		Theme.getColorScheme().mainColor = mainColor;
	}
	@Deprecated
	public static void setSecondColor(String secondColor) {
		Theme.getColorScheme().secondColor = secondColor;
	}
	@Deprecated
	public static void setTextColor(String txtColor) {
		Theme.getColorScheme().textColor = txtColor;
	}
	@Deprecated
	public static void setHeadingColor(String headColor) {
		Theme.getColorScheme().headingColor = headColor;
	}

}
