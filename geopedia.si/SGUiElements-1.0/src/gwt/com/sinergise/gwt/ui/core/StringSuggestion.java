/**
 * 
 */
package com.sinergise.gwt.ui.core;

import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class StringSuggestion implements Suggestion {
	final String str;
	final String repStr;
	public StringSuggestion(String str) {
		this.str = str;
		this.repStr = null;
	}
	public StringSuggestion(String str, String rep) {
		this.str = str;
		this.repStr = rep;
	}
	public String getDisplayString() {
		return str;
	}
	public String getReplacementString() {
		return repStr == null ? str : repStr;
	}
}