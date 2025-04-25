/**
 * 
 */
package com.sinergise.common.util.event.selection;

/**
 * @author tcerovski
 */
public interface ObjectSelectionListener<T> {
	
	public void objectSelected(T object, ObjectSelector<T> sender);
	
}
