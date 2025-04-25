package com.sinergise.generics.gwt.widgetprocessors;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.gwt.core.GenericWidget;
import com.sinergise.generics.gwt.core.WidgetProcessor;
import com.sinergise.generics.gwt.widgets.NoticeableWidgetWrapper;
import com.sinergise.generics.gwt.widgets.helpers.LookupGenericWidget;

public class MasonWidgetLookupBindingProcessor extends WidgetProcessor{

	
	protected Map<String,LookupGenericWidget> boundWidgets = 
		new HashMap<String,LookupGenericWidget>();
	
	@Override
	public Widget bind(Widget widget, int idx, GenericObjectProperty property,
			GenericWidget gw) {
		
		Map<String,String> metaAttributes = property.getAttributes();
		if (LookupGenericWidget.WIDGET_NAME.equals(metaAttributes.get(MetaAttributes.LOOKUP))) {	
			LookupGenericWidget lgw = null;

			if (widget instanceof NoticeableWidgetWrapper<?>) {
				Widget w = ((NoticeableWidgetWrapper<Widget>)widget).getWrappedWidget();
				 lgw = (LookupGenericWidget) w;
			} else {
				 lgw = (LookupGenericWidget) widget;
			}
			boundWidgets.put(property.getName(),lgw);
		}
		return widget;
	}

	public EntityObject getLookedupEntityObject(String name) {
		LookupGenericWidget lgw = boundWidgets.get(name);
		if (lgw==null)
			throw new RuntimeException("'"+name+"' attribute not found!");
		return lgw.getLookedUpEntityObject();
	}
}
