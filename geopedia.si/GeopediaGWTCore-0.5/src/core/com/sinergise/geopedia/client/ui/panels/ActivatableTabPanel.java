package com.sinergise.geopedia.client.ui.panels;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.geopedia.client.ui.LoadingIndicator;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGHeaderPanel;

public abstract class ActivatableTabPanel extends SGFlowPanel {
	
	
	private boolean isActive=false;
	private FlowPanel btnPanel;
	protected void internalActivate() {}
	protected boolean internalDeactivate() {return true;}
	private LoadingIndicator loadingIndicator;
	protected SGHeaderPanel mainWrap;
	
	
	public void showLoadingIndicator(boolean show) {
		if (loadingIndicator==null) {
			loadingIndicator = new LoadingIndicator(true, true);
			add(loadingIndicator);
		}
		loadingIndicator.setVisible(show);
	}
	public void activate() {
		if (isActive())
			return;
		internalActivate();
		isActive=true;		
	}

	public boolean canDeactivate() {
		return true;
	}
	public void deactivate() {
		if (!canDeactivate())
			return;
		if (!isActive())
			return;
		if (internalDeactivate()) {
			isActive=false;
		}
	}

	public boolean isActive() {
		return isActive;
	}
	
	protected InlineHTML titleLabel;
	private SGFlowPanel contentPanel;
	protected SGFlowPanel tabTitleWrapper;
	
	public ActivatableTabPanel(boolean isTitleVisible) {
		setHeight("100%");
		setStyleName("tabPanel");
		mainWrap= new SGHeaderPanel();
		
		tabTitleWrapper = new SGFlowPanel("tabPanelTitle clearfix");
		titleLabel = new InlineHTML();
		tabTitleWrapper.add(titleLabel);
		contentPanel = new SGFlowPanel("tabPanelContentHolder");
		if(isTitleVisible) {
			mainWrap.setHeaderWidget(tabTitleWrapper);
		}
		mainWrap.setContentWidget(contentPanel);
		add(mainWrap);
	}
	
	public ActivatableTabPanel() {
		this(true);
	}

	public void refresh() {}
	

	protected void setTabTitle(String title) {
		titleLabel.setHTML(title);
	}
	
	protected FlowPanel getButtonPanel() {
		return btnPanel;
	}
	protected void addButton(Widget button) {
		if (btnPanel==null) {
			btnPanel = new FlowPanel();
			btnPanel.setStyleName("buttonPanel");
			tabTitleWrapper.insert(btnPanel,0);
		}
		btnPanel.add(button);
	}
	
	public void setButtonsVisibility(boolean visible) {
		if (btnPanel!=null) {
			btnPanel.setVisible(visible);
		}
	}

	protected void addContent(Widget w) {
		contentPanel.add(w);
	}
	
	protected void clearContent() {
		contentPanel.clear();
	}

	
	protected void onDestroy() {		
	}

	public void setFooter(Widget w) {
		mainWrap.setFooterWidget(w);
	}
}
