package com.sinergise.generics.builder;


public abstract class AbstractDatabaseTableInspector implements Inspector{
	public static final int ENTITY_ATTRIBUTE_STARTPOSITION=100000;
	
	protected boolean ignoreAll = false;
	protected boolean loadRemarks = false;

	
	public AbstractDatabaseTableInspector setIgnoreAll(boolean ignore) {
		this.ignoreAll = ignore;
		return this;
	}
	
	public AbstractDatabaseTableInspector setLoadRemarks(boolean loadRemarks) {
		this.loadRemarks = loadRemarks;
		return this;
	}
}
