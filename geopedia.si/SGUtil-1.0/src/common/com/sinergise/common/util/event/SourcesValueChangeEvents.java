/**
 * 
 */
package com.sinergise.common.util.event;

/**
 * @author tcerovski
 */
public interface SourcesValueChangeEvents<T> {
	void addValueChangeListener(ValueChangeListener<? super T> l);
	
	void removeValueChangeListener(ValueChangeListener<? super T> l);
}
