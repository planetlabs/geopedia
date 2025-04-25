package com.sinergise.geopedia.core.common.util;

import java.io.Serializable;
import java.util.Collection;

public class PagableHolder<T extends Collection<?>> implements Serializable {
	private static final long serialVersionUID = 789019705527449912L;
	public static final int DATA_LOCATION_ALL = -1;

	private T collection;
	
	
	private boolean hasMoreData = false;
	
	public boolean hasMoreData() {
		return hasMoreData;
	}

	public int getTotalDataCount() {
		return totalDataCount;
	}

	public int getDataLocationStart() {
		return dataLocationStart;
	}

	public int getDataLocationEnd() {
		return dataLocationEnd;
	}

	private int totalDataCount = Integer.MIN_VALUE;
	private int dataLocationStart = Integer.MIN_VALUE;
	private int dataLocationEnd = Integer.MIN_VALUE;
	
	
	@Deprecated
	protected PagableHolder() {		
	}
	
	public PagableHolder(T collection, int start, int end, boolean hasMoreData) {
		this.dataLocationEnd = end;
		this.dataLocationStart  = start;
		this.hasMoreData=hasMoreData;
		this.collection = collection;
	}

	
	public PagableHolder(T collection, int start, int end, int totalDataCount) {
		this.dataLocationEnd = end;
		this.dataLocationStart  = start;
		this.totalDataCount = totalDataCount;
		if ((dataLocationEnd+1) < totalDataCount)
			hasMoreData=true;
		else
			hasMoreData=false;
		this.collection = collection;
	}

	public T getCollection() {
		return collection;
	}
}
