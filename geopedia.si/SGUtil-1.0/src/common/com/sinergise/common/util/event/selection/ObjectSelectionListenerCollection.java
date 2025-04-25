/**
 * 
 */
package com.sinergise.common.util.event.selection;

import java.util.ArrayList;

/**
 * @author tcerovski
 */
public class ObjectSelectionListenerCollection<T> extends ArrayList<ObjectSelectionListener<T>> {
	
	private static final long serialVersionUID = -731894456650189744L;
	
	public void fireObjectSelected(final T object, final ObjectSelector<T> sender) {
		for (final ObjectSelectionListener<T> listener : this) {
			listener.objectSelected(object, sender);
		}
	}
	
}
