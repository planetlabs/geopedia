package com.sinergise.geopedia.pro.client.ui;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.StackPanel;

public class SettingsStack extends StackPanel{

	public class StackTitle extends FlowPanel {
		
		private Label title;
		private Label details;
		public StackTitle(String text) {
			title = new Label(text);
			title.setStyleName("title");
			details = new Label();
			details.setStyleName("details");
			add(title);
			add(details);
		}
		
		public void setTitle(String text) {
			title.setText(text);
		}
		
		public void setDetails(String text) {
			details.setText(text);
		}
	}
	
	private String getTitleHTML(AbstractSettingsStackPanel pnl) {
		return getTitleHTML(pnl.getTitleText(), pnl.getTitleDetails());
	}
	private String getTitleHTML(String titleText,String detailsText) {		
		String rv  ="<div class=\"title\">"+
			"<span class=\"titleText\">"+titleText+"</span>";
		if (detailsText!=null && detailsText.trim().length()>0) {
			rv+="<span class=\"detailsText\">"+detailsText+"</span>";
		}
		rv+="</div>";
		return rv;
			
	}
	public void updateStackText(AbstractSettingsStackPanel panel) {
		int idx = getWidgetIndex(panel);
		if (idx==-1) return;
		setStackText(idx, getTitleHTML(panel),true);
	}
	
	
	
	public void add(AbstractSettingsStackPanel panel) {
		panel.setSeettingsStack(this);
		add(panel, getTitleHTML(panel),true);
	}
	
	public SettingsStack() {
		setStyleName("settingsStack");
	}
	
}
