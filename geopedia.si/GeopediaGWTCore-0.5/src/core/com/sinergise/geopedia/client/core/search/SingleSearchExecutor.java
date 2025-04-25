package com.sinergise.geopedia.client.core.search;

import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;

public class SingleSearchExecutor implements SearchListener, SearchExecutor {
	private SearchListener realSearchListener;
	
	private boolean searchRunning = false;
	private Searcher delayedSearcher = null;
		
	public void setSearchListener (SearchListener sl) {
		realSearchListener = sl;
	}
	
	public void executeSearch(Searcher searcher) {
		if (searchRunning) {
			delayedSearcher = searcher;
		} else {
			searchRunning = true;
			searcher.search(this);
		}
	}
	
	public void resetSearch() {
		systemNotification(SystemNotificationType.GLOBAL_CLEAR_RESULTS,null);
	}

	private void handleNotifications(SystemNotificationType type) {
		if (type == SystemNotificationType.GLOBAL_SEARCH_DONE  || type == SystemNotificationType.ERROR) {
			if (delayedSearcher!=null) {
				searchRunning=true;
				Searcher srch = delayedSearcher;
				delayedSearcher = null;
				srch.search(this);
			} else {
				searchRunning=false;
			}
		}
	}
	@Override
	public void systemNotification(SystemNotificationType type,
			String message) {
		handleNotifications(type);
		if (realSearchListener!=null)
			realSearchListener.systemNotification(type, message);
	}

	@Override
	public void systemNotification(SystemNotificationType type, Table t,
			String message) {
		handleNotifications(type);
		if (realSearchListener!=null)
			realSearchListener.systemNotification(type, t, message);
	}

	@Override
	public void searchResults(Feature[] features, Table table,
			boolean hasMoreData, boolean error, String errorMessage) {
		if (realSearchListener!=null)
			realSearchListener.searchResults(features, table, hasMoreData, error, errorMessage);
	}

	@Override
	public void themesSearchResults(Theme[] themes, boolean error,
			String errorMessage) {
		if (realSearchListener!=null)
			realSearchListener.themesSearchResults(themes, error, errorMessage);
	}

	@Override
	public void tablesSearchResults(Table[] tables, boolean error,
			String errorMessage) {
		if (realSearchListener!=null)
			realSearchListener.tablesSearchResults(tables, error, errorMessage);		
	}

}
