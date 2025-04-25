package com.sinergise.gwt.ui.action;

import com.sinergise.common.ui.action.Action;
import com.sinergise.gwt.util.history.HistoryManager;

/**
 * Action button to add or remove a history parameter.
 * 
 * @author tcerovski
 */
public class HistoryAction extends Action {
	
	private boolean removeOnAction = true;
	public final String historyParamKey;

	public HistoryAction(String nameAndParamKey) {
		this(nameAndParamKey, nameAndParamKey);
	}
	
	public HistoryAction(String nameAndParamKey, boolean removeOnAction) {
		this(nameAndParamKey);
		this.removeOnAction = removeOnAction;
	}
	
	public HistoryAction(String name, String historyParamKey) {
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
	protected void actionPerformed() {
		//add the parameter to the history
		HistoryManager.getInstance().setHistoryParam(historyParamKey, getHistoryParamValue());
		if (removeOnAction) {
			//and then remove it - all handlers will be triggered with the changed to this point as JS is thread safe
			HistoryManager.getInstance().removeHistoryParam(historyParamKey);
		}
	}

}
