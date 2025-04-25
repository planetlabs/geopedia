package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;

public class SGLabel extends Label {
	public SGLabel(String text, String stylePrimaryName, final ClickHandler ch) {
		super(text);
		setStylePrimaryName(stylePrimaryName);
		addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ch.onClick(event);
				event.preventDefault();
				event.stopPropagation();
			}
		});
	}
}
