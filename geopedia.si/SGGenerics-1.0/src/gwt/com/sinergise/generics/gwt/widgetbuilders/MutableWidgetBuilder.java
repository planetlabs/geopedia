package com.sinergise.generics.gwt.widgetbuilders;

import java.util.Map;

import com.google.gwt.user.client.ui.Widget;

public interface MutableWidgetBuilder extends WidgetBuilder {
	public Object getWidgetValue(Widget widget, String attributeName, Map<String,String> metaAttributes);
	public void setWidgetValue(Widget widget, String attributeName, Map<String,String> metaAttributes, Object value);
	public void updateWidgetMetaAttribute(Widget widget, Map<String,String> metaAttributes, String metaAttribute, String value);
	public CustomAttributeWidget<String> getCustomAtributeWidget (String attributeName, Map<String,String> metaAttributes); 
}
