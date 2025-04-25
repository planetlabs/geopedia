package com.sinergise.gwt.util.history;

import java.util.Collection;

/**
 * @author tcerovski
 *
 */
public interface HistoryHandler {

	public Collection<String> getHandledHistoryParams();
	
	public void handleHistoryChange(HistoryManager manager);
	
}
