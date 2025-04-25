package com.sinergise.geopedia.client.core.search;

import java.util.ArrayList;

import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;

public class SearchResultsDistributor implements SearchListener{

	private ArrayList<SearchListener> listeners = new ArrayList<SearchListener>();
	public void addSearchListener(SearchListener l) {
		listeners.add(l);
	}
	public void removeSearchListener (SearchListener l) {
		listeners.remove(l);
	}
	
	@Override
	public void systemNotification(SystemNotificationType type, String message) {
		for (SearchListener l:listeners)
			l.systemNotification(type, message);
		
	}

	@Override
	public void systemNotification(SystemNotificationType type, Table t,
			String message) {
		for (SearchListener l:listeners)
			l.systemNotification(type, t, message);
		
	}

	
	@Override
	public void searchResults(Feature[] features, Table table,
			boolean hasMoreData, boolean error, String errorMessage) {
		for (SearchListener l:listeners) {
			l.searchResults(features, table, hasMoreData, error, errorMessage);
		}		
	}
	@Override
	public void themesSearchResults(Theme[] themes, boolean error,
			String errorMessage) {
		for (SearchListener l:listeners) {
			l.themesSearchResults(themes, error, errorMessage);
		}				
	}
	@Override
	public void tablesSearchResults(Table[] tables, boolean error,
			String errorMessage) {
		for (SearchListener l:listeners) {
			l.tablesSearchResults(tables, error, errorMessage);
		}	
	}

}
