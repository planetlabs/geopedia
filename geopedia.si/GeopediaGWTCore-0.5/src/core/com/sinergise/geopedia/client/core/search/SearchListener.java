package com.sinergise.geopedia.client.core.search;

import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.core.entities.Theme;

public interface SearchListener {
	public enum SystemNotificationType { ERROR, MESSAGE,
		TABLE_SEARCH_START, TABLE_SEARCH_DONE, TABLE_SEARCH_FAIL, TABLE_SEARCH_HAS_MORE_DATA,
		GLOBAL_SEARCH_START, GLOBAL_SEARCH_DONE, GLOBAL_CLEAR_RESULTS}; 
	void systemNotification(SystemNotificationType type, String message);
	void systemNotification(SystemNotificationType type, Table t, String message);
	void searchResults(Feature[] features, Table  table, boolean hasMoreData, boolean error, String errorMessage);
	void themesSearchResults(Theme[] themes, boolean error, String errorMessage);
	void tablesSearchResults(Table[] tables, boolean error, String errorMessage);
}
