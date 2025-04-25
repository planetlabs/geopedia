package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.ui.resources.Theme;

public class SGTitledHeaderPanel extends SGHeaderPanel {

	private SGFlowPanel headerHolder = new SGFlowPanel(Theme.getTheme().layoutBundle().layoutComponents().sgTitledHeaderPanel_head());
	private InlineLabel headerLabel;
	
	public SGTitledHeaderPanel() {
		Theme.getTheme().layoutBundle().layoutComponents().ensureInjected();
		addStyleName(Theme.getTheme().layoutBundle().layoutComponents().sgTitledHeaderPanel());
	}
	public SGTitledHeaderPanel(String headerTitle) {
		this();
		setTitleText(headerTitle);
	}
	public SGTitledHeaderPanel(String headerTitle, Widget contentWidget) {
		this(headerTitle);
		setWidget(contentWidget);
	}
	public SGTitledHeaderPanel(Widget headerWidget, Widget contentWidget) {
		this();
		setTitleWidget(headerWidget);
		setWidget(contentWidget);
	}
	
	
	public void setTitleText(String headerTitle) {
		if(headerLabel==null) {
			headerLabel = new InlineLabel(headerTitle);
			headerHolder.add(headerLabel);
		}
		headerLabel.setText(headerTitle);
		setHeaderWidget(headerHolder);
	}
	
	public void setTitleWidget(Widget headerWidget) {
		headerHolder.add(headerWidget);
		setHeaderWidget(headerHolder);
	}
	public void setWidget(Widget contentWidget) {
		setContentWidget(contentWidget);
		contentWidget.addStyleName(Theme.getTheme().layoutBundle().layoutComponents().sgTitledHeaderPanel_content());
	}
	
	public void addHeaderWidget(Widget addedWidget, String styleName) {
		headerHolder.insert(addedWidget,0);
		addedWidget.addStyleName(styleName);
		headerHolder.addStyleName(Theme.getTheme().layoutBundle().layoutComponents().sgTitledHeaderPanel_head_with_rightWidget());
	}
	public void addHeaderWidget(Widget addedWidget, HorizontalAlignmentConstant align) {
		headerHolder.insert(addedWidget,0);
		addedWidget.addStyleName(align.getTextAlignString());
		headerHolder.addStyleName(Theme.getTheme().layoutBundle().layoutComponents().sgTitledHeaderPanel_head_with_rightWidget());
	}
}
