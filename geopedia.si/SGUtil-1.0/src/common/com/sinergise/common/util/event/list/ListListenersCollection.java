/*
 *
 */
package com.sinergise.common.util.event.list;

import java.util.Vector;

public class ListListenersCollection extends Vector<ListListener> {
	private static final long serialVersionUID = 1L;
	
	public void fireItemAdded(final Object item, final int newIndex) {
		for (final ListListener listListener : this) {
			(listListener).itemAdded(item, newIndex);
		}
	}
	
	public void fireItemRemoved(final Object item, final int oldIndex) {
		for (final ListListener listListener : this) {
			(listListener).itemRemoved(item, oldIndex);
		}
	}
	
	public void fireItemChanged(final Object item, final int index) {
		for (final ListListener listListener : this) {
			(listListener).itemChanged(item, index);
		}
	}
}
