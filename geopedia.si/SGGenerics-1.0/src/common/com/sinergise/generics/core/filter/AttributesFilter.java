package com.sinergise.generics.core.filter;

import com.sinergise.generics.core.filter.predicate.IEntityObjectFilter;

public class AttributesFilter implements DataFilter {

	
	private static final long serialVersionUID = 2415729264798803609L;

	private int entityTypeId = Integer.MIN_VALUE;
	private IEntityObjectFilter filter;
	
	
	@Deprecated
	protected AttributesFilter() {
	}
	
	
	public AttributesFilter(IEntityObjectFilter filter) {
		this.filter=filter;
	}
	
	public IEntityObjectFilter getFilter() {
		return filter;
	}
	public void setEntityTypeId(int id) {
		this.entityTypeId = id;
	}
	
	public int getEntityTypeId() {
		return entityTypeId;
	}
}
