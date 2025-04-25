package com.sinergise.common.util.event.update;

public interface SourcesUpdateEvents {
	/**
	 * Adds a listener interface.
	 * 
	 * @param listener the listener interface to add
	 */
	public void addUpdateListener(UpdateListener listener);
	
	/**
	 * Removes a previously added listener interface.
	 * 
	 * @param listener the listener interface to remove
	 */
	public void removeUpdateListener(UpdateListener listener);
}
