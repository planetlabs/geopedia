package com.sinergise.generics.core.filter;

import java.util.ArrayList;

import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;

public class CompoundEntityFilter implements DataFilter{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6681260271865312143L;
	private ArrayList<SimpleFilter> filters = new ArrayList<SimpleFilter>();
	
	public CompoundEntityFilter() {
	}
	
	public boolean isValid() {
		if (filters.size()>0)
			return true;
		return false;
	}
	
	public EntityType getEntityType() {
		if (!isValid())
			return null;
		for (SimpleFilter ff:filters) { // search through filters and try to find one that contains valid data.
			EntityObject fd = ff.getFilterData();
			if (fd!=null)
				return fd.getType();
		}
		return null;
	}
	
	
	public void addFilter (SimpleFilter filter) {	
		// add checking !!
		filters.add(filter);
	}
	
	public SimpleFilter[] getFilters() {
		return filters.toArray(new SimpleFilter[filters.size()]);
	}
	
	
}
