package com.sinergise.geopedia.core.filter;

import java.io.Serializable;

import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.geopedia.core.common.util.StringUtils;

public abstract class AbstractFilter implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final int TYPE_LISTFILTER = 1;
	
	public static final String STATE_KEY_FILTERTYPE="filterType";
	public static final String STATE_KEY_TABLEID="tableId";
	
	public static final String STATE_KEY_FILTER_COUNT="filterCount";
	public static final String STATE_KEY_FILTER_BASE="filter";

	public int filterType;
	public int tableId;
	
	
	public static void storeFilters(AbstractFilter[] filters, StateGWT state) {
		int filterCount = 0;
		if (filters!=null && filters.length>0) {
			filterCount=filters.length;
			for (int i=0;i<filterCount;i++) {
				state.putState(STATE_KEY_FILTER_BASE+i,filters[i].getState());
			}
		}
		state.putInt(STATE_KEY_FILTER_COUNT, filterCount);
	}
	public static AbstractFilter[] createFilters(StateGWT state) {
		int filterCount = state.getInt(STATE_KEY_FILTER_COUNT, 0);
		AbstractFilter[] filters = null;
		if (filterCount>0) {
			filters = new AbstractFilter[filterCount];
			for (int i=0;i<filterCount;i++) {
				filters[i] = AbstractFilter.createFromState(state.getState(STATE_KEY_FILTER_BASE+i));
			}
		}
		return filters;
	}
	
	public static AbstractFilter createFromState(StateGWT state) {
		int filterType = state.getInt(STATE_KEY_FILTERTYPE, Integer.MIN_VALUE);
		if (filterType == Integer.MIN_VALUE)
			throw new IllegalArgumentException("State does not contain '"+STATE_KEY_FILTERTYPE+"' attribute!");
		if (filterType==TYPE_LISTFILTER)
			return new ListFilter(state);
		
		throw new IllegalArgumentException("Unsupported filter type '"+filterType+"'");
	}
	
	public static AbstractFilter createFromString (String filterString) {
		String args[] = StringUtils.parseArray(filterString);
		if (args==null ||args.length!=3)
			throw new IllegalArgumentException("Error parsing filter data '"+filterString+"'");
		try {
			int filterType = Integer.parseInt(args[0]);
			int tableId = Integer.parseInt(args[1]);
			switch (filterType) {
				case TYPE_LISTFILTER:
					return new ListFilter(tableId, args[2]);
				default:
					throw new IllegalArgumentException("Unsupported filter type '"+filterType+"'");
			}
			
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Error parsing filter data '"+filterString+"'");
		}
	}

	protected void loadState(StateGWT state) {
		tableId = state.getInt(STATE_KEY_TABLEID, Integer.MIN_VALUE);
		filterType = state.getInt(STATE_KEY_FILTERTYPE, Integer.MIN_VALUE);
	}
	
	public StateGWT getState() {
		StateGWT state = new StateGWT();
		state.putInt(STATE_KEY_FILTERTYPE, filterType);
		state.putInt(STATE_KEY_TABLEID, tableId);
		
		return state;
	}
}
