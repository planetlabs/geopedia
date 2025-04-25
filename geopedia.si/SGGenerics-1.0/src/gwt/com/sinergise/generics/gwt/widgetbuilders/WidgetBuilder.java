package com.sinergise.generics.gwt.widgetbuilders;

import java.util.Map;

import com.google.gwt.user.client.ui.Widget;

public interface WidgetBuilder {
	public Widget buildWidget (String attributeName, Map<String,String> metaAttributes);
}
