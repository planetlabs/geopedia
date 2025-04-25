package com.sinergise.gwt.ui.maingui.extwidgets;

import com.sinergise.gwt.ui.maingui.SimpleLoadingPanel;

public class SGFlowPanelWithLoading extends SGFlowPanel {

	protected SimpleLoadingPanel loadingPanel;
	
	public void showLoading(){
		if(loadingPanel == null){
			loadingPanel = new SimpleLoadingPanel(this);
		}
		loadingPanel.showLoading();
	}
	
	public void hideLoading(){
		if(loadingPanel != null){
			loadingPanel.hideLoading();
		}
	}
	
}
