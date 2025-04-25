package com.sinergise.gwt.ui.action;

import com.sinergise.common.ui.action.ToggleAction;
import com.sinergise.gwt.util.history.HistoryManager;


/**
 * Toggle action button to add or remove a history parameter.
 * 
 * @author tcerovski
 */
public class HistoryToggleAction extends ToggleAction {
	
	public final String historyParamKey;
	
	public HistoryToggleAction(String nameAndParamKey) {
		this(nameAndParamKey, nameAndParamKey);
	}
	
	public HistoryToggleAction(String name, String historyParamKey) {
		super(name);
		this.historyParamKey = historyParamKey;
	}
	
	/**
	 * @return null by default, but could be override to provide different value
	 */
	protected String getHistoryParamValue() {
		return null;
	}

	@Override
	protected void selectionChanged(boolean newSelected) {
		if (newSelected) {
			HistoryManager.getInstance().setHistoryParam(historyParamKey, getHistoryParamValue());
		} else {
			HistoryManager.getInstance().removeHistoryParam(historyParamKey);
		}
	}

}
