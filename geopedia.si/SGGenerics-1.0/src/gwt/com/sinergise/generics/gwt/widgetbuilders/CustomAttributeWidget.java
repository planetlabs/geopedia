package com.sinergise.generics.gwt.widgetbuilders;

import java.util.Map;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.user.client.ui.Composite;
import com.sinergise.gwt.ui.core.IInputWidget;

public abstract class CustomAttributeWidget<T> extends Composite implements  HasValueChangeHandlers<T>, IInputWidget {

	public abstract T getWidgetValue(Map<String, String> metaAttributes);
	public abstract void setWidgetValue(Map<String, String> metaAttributes, T value);
}
