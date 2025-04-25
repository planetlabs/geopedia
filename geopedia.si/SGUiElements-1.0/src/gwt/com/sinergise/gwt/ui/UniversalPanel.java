package com.sinergise.gwt.ui;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sinergise.gwt.ui.maingui.ILoadingWidget;

public class UniversalPanel extends FlowPanel implements ILoadingWidget {

	
	private SimplePanel loadingPanel = null;
	private SimplePanel titlePanel = null;
	  /**
	   * Creates an empty flow panel.
	   */
	  public UniversalPanel() {
		  super();
	  }

	  
	  
	  @Override
	public void setTitle(String title) {
		  if (title==null) {
			  if (titlePanel!=null) {
				  remove(titlePanel);
				  titlePanel=null;
			  }
		  } else {			  
			  if (titlePanel==null) {
				  titlePanel = new SimplePanel();
				  titlePanel.setStyleName("title");
				  insert(titlePanel,0);
			  }
			  titlePanel.getElement().setInnerHTML(title);
		  }
	  }
	  
	  public void enableLoading(boolean enable) {
		  if (enable) {
			if (loadingPanel != null)  
				return;
			loadingPanel = new SimplePanel();
			loadingPanel.setStyleName("widgetLoading");
		    add(loadingPanel);
		  } else {
			  remove(loadingPanel);
			  loadingPanel = null;
		  }
		  
	  }
	  
	  @Override
	  public void clear() {
		  super.clear();
		  if (titlePanel!=null)
			  insert(titlePanel,0);
		  if (loadingPanel!=null)			  
			  add(loadingPanel);
	  }

	  public void showLoading(int idx) {
		  if (loadingPanel!=null)
			  loadingPanel.addStyleDependentName("show");
	  }
	  public void hideLoading(){
		  if (loadingPanel!=null)
			  loadingPanel.removeStyleDependentName("show");
	  }
	  
	}
