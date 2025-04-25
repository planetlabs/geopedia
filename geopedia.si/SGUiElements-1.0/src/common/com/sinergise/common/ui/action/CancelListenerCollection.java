/*
 *
 */
package com.sinergise.common.ui.action;

import java.util.Vector;

public class CancelListenerCollection extends Vector<CancelListener> {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Fires a change event to all listeners.
     * 
     * @param sender
     *            the widget sending the event.
     */
    public void fireCancel(Object sender) {
    	for (CancelListener cl : this) {
            cl.cancel(sender);
        }
    }
}
