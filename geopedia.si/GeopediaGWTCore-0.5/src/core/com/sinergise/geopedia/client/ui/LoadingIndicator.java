package com.sinergise.geopedia.client.ui;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.sinergise.geopedia.client.resources.GeopediaCommonStyle;

public class LoadingIndicator extends FlowPanel {
	private InlineLabel textLbl;
	
	public LoadingIndicator() {
		add(new Image(GeopediaCommonStyle.INSTANCE.loading()));
		setStyleName("loadingIndicator");
	}
	public LoadingIndicator(boolean clean) {
		this();
		addStyleName("clean");
	}
	public LoadingIndicator(boolean clean, boolean fullFrame) {
		this(clean);
		addStyleName("fullFrame");
	}
	public LoadingIndicator(String text) {
		this();
		setLoadingText(text);
	}
	
	public void setLoadingText(String text) {
		if (textLbl == null) {
			textLbl = new InlineLabel();
			add(textLbl = new InlineLabel(text));
		} else {
			textLbl.setText(text);
		}
	}
}
