package com.sinergise.common.util.event.selection;

public interface ObjectSelector<T> {
	void addSelectionListener(ObjectSelectionListener<T> listener);
	
	void removeSelectionListener(ObjectSelectionListener<T> listener);
}
