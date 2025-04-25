package com.sinergise.generics.gwt.widgetprocessors;

import com.google.gwt.user.client.ui.Widget;

public class ValidationResults {
	public Widget widget;
	public String message;
	
	public ValidationResults(Widget w) {
		this.widget=w;
	}
}
