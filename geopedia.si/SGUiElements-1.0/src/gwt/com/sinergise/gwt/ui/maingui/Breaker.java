package com.sinergise.gwt.ui.maingui;

import com.google.gwt.user.client.ui.SimplePanel;

public class Breaker extends SimplePanel {

	public Breaker() {
		setStyleName("breaker");
	}
	public Breaker(int size) {
		this();
		setHeight(size+"px");
	}
}
