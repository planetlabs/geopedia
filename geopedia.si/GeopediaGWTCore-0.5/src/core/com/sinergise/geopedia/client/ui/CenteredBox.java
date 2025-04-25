package com.sinergise.geopedia.client.ui;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.geopedia.client.ui.base.CenteredWidget;
import com.sinergise.gwt.ui.Heading;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;

public class CenteredBox extends CenteredWidget {
	
	private Heading title;
	private SGFlowPanel header;
	protected SGFlowPanel footer;
	private SGFlowPanel btnPanel;
	
	public CenteredBox() {
		super(true, false);
		addStyleName("centeredBox");
		autoClose();
		header = new SGFlowPanel("boxHeader");
		header.add(title = new Heading.H1(""));
		SimplePanel shadow = new SimplePanel();
		shadow.setStyleName("shadow");
		header.add(shadow);
		
		footer = new SGFlowPanel("boxFooter");
		SimplePanel shadow1 = new SimplePanel();
		shadow1.setStyleName("shadow");
		footer.add(shadow1);
		
		btnPanel = new SGFlowPanel("btnPanel");
		footer.add(btnPanel);
		innerContent.add(header);
		innerContent.add(footer);
	}
	
	public CenteredBox(Widget w) {
		this();
		setContent(w);
	}
	
	public void setHeaderTitle(String titleText) {
		title.setText(titleText);
	}
	
	public void clearHeaderButtons() {
		btnPanel.clear();
	}
	public void addHeaderButton(Widget w) {
		btnPanel.add(w);
	}
	
	public SGFlowPanel getButtonsHolder() {
		return btnPanel;
	}
}
