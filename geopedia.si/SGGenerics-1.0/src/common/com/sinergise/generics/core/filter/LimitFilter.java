package com.sinergise.generics.core.filter;

import com.sinergise.common.util.settings.Settings;

/**
 * Provisional implementation of limit filter, should be revisited
 * @author bpajntar
 *
 */
public class LimitFilter implements DataFilter, Settings {

	private static final long serialVersionUID = -1854600791498850262L;

	
	protected int limitNumber = Integer.MIN_VALUE;


	
	protected LimitFilter() {		
	}
	
	
	public LimitFilter(int limit) {
		limitNumber = limit;
	}


	public int getLimitNumber() {
		return limitNumber;
	}


	public void setLimitNumber(int limitNumber) {
		this.limitNumber = limitNumber;
	}
	
	
	
	
}
