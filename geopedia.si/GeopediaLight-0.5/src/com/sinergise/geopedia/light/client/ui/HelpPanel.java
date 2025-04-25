package com.sinergise.geopedia.light.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.NotificationPanel;
import com.sinergise.gwt.ui.resources.Theme;

public class HelpPanel extends NotificationPanel {
	
	private ImageAnchor closeAnchor; 
	
	public HelpPanel() {
		addStyleName("helpPanel");
		show();
		setType(MessageType.INFO);
		closeAnchor = new ImageAnchor(Theme.getTheme().standardIcons().close());
		add(closeAnchor);
	}
	public HelpPanel(String message) {
		this();
		setHTMLMessage(message);
		
		ClickHandler removeFromParent = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				HelpPanel.this.removeFromParent();
			}
		};
		setHandler(removeFromParent);
		
	}
	public HelpPanel(String message, ClickHandler handler) {
		this(message);
		setHandler(handler);
	}
	
	public void setHandler(ClickHandler handler) {
		closeAnchor.addClickHandler(handler);
	}
}
