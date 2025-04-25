/*
 *
 */
package com.sinergise.common.util.event.selection;

import java.util.Vector;

public class ToggleListenerCollection extends Vector<ToggleListener> {
	/**
     * 
     */
	private static final long serialVersionUID = -7234285389310929785L;
	
	/**
	 * Fires a change event to all listeners.
	 * 
	 * @param sender the widget sending the event.
	 */
	public void fireActionPerformed(final SourcesToggleEvents sender, final boolean oldOn, final boolean newOn) {
		if (oldOn == newOn) {
			return;
		}
		for (final ToggleListener tl : this) {
			tl.toggleStateChanged(sender, newOn);
		}
	}
}
