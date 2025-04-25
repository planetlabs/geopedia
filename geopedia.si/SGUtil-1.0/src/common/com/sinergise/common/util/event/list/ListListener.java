/*
 *
 */
package com.sinergise.common.util.event.list;

public interface ListListener {
	void itemAdded(Object item, int newIndex);
	
	void itemRemoved(Object item, int oldIndex);
	
	void itemChanged(Object item, int index);
}
