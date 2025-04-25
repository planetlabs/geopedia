package com.sinergise.gwt.util.history;

/**
 * @author tcerovski
 *
 */
public class SimpleHistoryLink extends HistoryLink {
	
	private final String simpleParamKey;

	public SimpleHistoryLink(String text, String paramKey, String paramValue) {
		this(text, false, paramKey, paramValue);
	}
	
	public SimpleHistoryLink(String text, boolean asHtml, String paramKey, String paramValue) {
		super(text, asHtml, new String[]{paramKey}, new String[]{paramValue});
		this.simpleParamKey = paramKey;
	}
	
	public String getParamValue() {
		return getParamValue(simpleParamKey);
	}
	
	public void setParamValue(String value) {
		setParamValue(simpleParamKey, value);
	}
	
	public String getParamKey() {
		return simpleParamKey;
	}
	
}
