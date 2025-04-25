package com.sinergise.common.util.event.update;

import java.util.Vector;

public class UpdateListenerCollection extends Vector<UpdateListener> {
	/**
     * 
     */
	private static final long serialVersionUID = -7751285389310929785L;
	
	/**
	 * Fires update event to all listeners.
	 * 
	 * @param sender the widget sending the event.
	 */
	public void fireUpdateConfirmed(final Object sender) {
		for (final UpdateListener pcl : this) {
			pcl.itemUpdateConfirmed(sender);
		}
	}
	
	/**
	 * Fires update start event to all listeners.
	 * 
	 * @param sender the widget sending the event.
	 */
	public void fireUpdateStarted(final Object sender) {
		for (final UpdateListener pcl : this) {
			pcl.itemUpdateStarted(sender);
		}
	}
	
	/**
	 * Fires cancel event to all listeners.
	 * 
	 * @param sender the widget sending the event.
	 */
	public void fireUpdateCancelled(final Object sender) {
		for (final UpdateListener pcl : this) {
			pcl.itemUpdateCancelled(sender);
		}
	}
}
