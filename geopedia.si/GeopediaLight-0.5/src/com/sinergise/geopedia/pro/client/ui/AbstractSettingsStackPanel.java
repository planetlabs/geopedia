package com.sinergise.geopedia.pro.client.ui;

import com.google.gwt.user.client.ui.FlowPanel;

public class AbstractSettingsStackPanel extends FlowPanel {
	
	protected String  titleText;
	protected String titleDetails;
	protected SettingsStack stack;
	
	public String getTitleText() {
		return titleText;
	}
	public String getTitleDetails() {
		return titleDetails;
	}
	
	public void setSeettingsStack(SettingsStack stack) {
		this.stack=stack;
	}
	
	protected void updateTitle() {
		if (stack!=null) {
			stack.updateStackText(this);
		}
	}

}
