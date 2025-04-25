package com.sinergise.geopedia.client.ui.panels;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.sinergise.geopedia.client.core.i18n.ExceptionI18N;
import com.sinergise.gwt.ui.resources.Theme;

public class StatusPanel extends FlowPanel {
	private FlowPanel content;
	
	private enum PanelType {ERROR,WARNING, INFO};
	
	public StatusPanel() {
		setStyleName("statusPanel");
		content = new FlowPanel();
		content.setStyleName("content");
		add(content);
		reset();
	
	}
	
	
	public void reset() {
		content.clear();
		content.setVisible(false);
	}
	
	
	public void addPanel(PanelType type, String text, boolean show) {
		FlowPanel panel = new FlowPanel();
		panel.setStyleName("type"+type.name());
		ImageResource imgRes = null;
		if (type == PanelType.ERROR) {
			imgRes= Theme.getTheme().standardIcons().error();
		} else if (type == PanelType.WARNING) {
			imgRes= Theme.getTheme().standardIcons().warning();
		} else {
			imgRes= Theme.getTheme().standardIcons().info();
		}
		panel.add(new Image(imgRes));
		panel.add(new Label(text));

		content.add(panel);
		if (show)
			content.setVisible(true);

	}
	
	
	public void addException(Throwable th) {
		addException(th,true);
	}
	public void addException(Throwable th, boolean show) {
		addPanel(PanelType.ERROR, ExceptionI18N.getLocalizedMessage(th), show);
	}
}
