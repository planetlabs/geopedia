package com.sinergise.gwt.ui.maingui.extwidgets;

import com.sinergise.gwt.ui.maingui.tabs.SGFlowFormTab;

public abstract class SimpleSearchResultsTab extends SGFlowFormTab {

	protected boolean    isPinned;
	
	protected Object     searchParams;
	
	public void doSearch(Object sp) {
		this.searchParams = sp;
	}
	
	public boolean isPinned() {
		return isPinned;
	}

	public void setPinned(boolean isPinned) {
		this.isPinned = isPinned;
	}
	
}
