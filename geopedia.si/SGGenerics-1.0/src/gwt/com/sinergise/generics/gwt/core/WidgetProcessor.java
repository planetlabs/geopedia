package com.sinergise.generics.gwt.core;

import com.sinergise.generics.core.GenericObjectProperty;

/**
 * Abstract class, that only exposes methods that are mandatory to implement most of the time.
 * 
 * @author pkolaric
 *
 * TODO: rename to AbstractWidgetProcessor
 */
public abstract class WidgetProcessor implements IWidgetProcessor {

	
	@Override
	public boolean unBind(int idx, GenericObjectProperty property) {
		return false;
	}
	
	
}
