package com.sinergise.geopedia.client.ui.panels;

import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;

public abstract class ActivatableStackPanel extends SGFlowPanel {
	
	protected StackableTabPanel container;
	
	private boolean showLoading = false;
	
	public void showLoadingIndicator (boolean show) {
		this.showLoading=show;
		if (container!=null) {
			container.showLoadingIndicator(show);
		}
	}
	
	public boolean canDeactivate() {
		return true;
	}

	public void onActivate() {}
	public void onDeactivate() {};
	
	
	public void setContainer(StackableTabPanel container) {
		this.container = container;
		if (showLoading) {
			showLoadingIndicator(showLoading);
		}
	}
	protected void deactivate() {
		container.removeStackPanel(this);
		container.onResize();
	}
}
