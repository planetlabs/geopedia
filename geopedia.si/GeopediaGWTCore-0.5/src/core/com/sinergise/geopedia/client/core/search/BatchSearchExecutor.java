package com.sinergise.geopedia.client.core.search;

import java.util.ArrayList;

import com.sinergise.geopedia.client.core.search.SearchListener.SystemNotificationType;

public class BatchSearchExecutor implements Searcher{
	
	private ArrayList<BatchSearcher> searchers = new ArrayList<BatchSearcher>();
	private int searcherIdx = 0;
	private boolean searchPending = false;
	private SearchListener listener;
	
	public void addSearcher(BatchSearcher searcher) {
		searchers.add(searcher);
	}

	public void addSearchers(BatchSearcher[] srchrs) {
		for (BatchSearcher s:srchrs) {
			searchers.add(s);
		}
	}
	
	public void searchDone() {
		if ((searcherIdx+1)<searchers.size()) {
			searcherIdx++;
			searchers.get(searcherIdx).execute(listener, this);
		}else {
			listener.systemNotification(SystemNotificationType.GLOBAL_SEARCH_DONE,"");
		}
		//TODO implement cancel
	}
	
	
	@Override
	public void search(SearchListener listener) {
		if (searchPending)
			return;
		searchPending=true;
		this.searcherIdx=0;
		this.listener=listener;
		listener.systemNotification(SystemNotificationType.GLOBAL_SEARCH_START,"");
		searchers.get(searcherIdx).execute(listener, this);
	}
	
	
}
