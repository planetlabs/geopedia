package com.sinergise.generics.gwt.core;

import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.GenericObjectProperty;

public interface IWidgetProcessor {
	public Widget bind(Widget widget, int idx, GenericObjectProperty property, GenericWidget gw);

	/**
	 * Implement if GenericWidget supports unbinding (deleting it's widgets)
	 * Returns true as soon the last bound property is released.
	 * 
	 * return 
	 */
	public boolean unBind(int idx, GenericObjectProperty property);
	
}
