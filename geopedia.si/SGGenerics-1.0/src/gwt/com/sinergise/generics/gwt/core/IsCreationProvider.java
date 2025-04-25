package com.sinergise.generics.gwt.core;

public interface IsCreationProvider {
	/**
	 * Returns true if creation provider's creation is finished (after CreationListener.creationCompleted has been called),
	 * false otherwise.
	 * 
	 * @return
	 */
	public boolean isCreated();
	public void addCreationListener(CreationListener l);
}
