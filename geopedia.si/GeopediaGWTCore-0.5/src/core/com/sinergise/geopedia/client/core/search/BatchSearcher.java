package com.sinergise.geopedia.client.core.search;

public abstract class BatchSearcher {
	public abstract void execute(SearchListener listener, BatchSearchExecutor executor);
}
