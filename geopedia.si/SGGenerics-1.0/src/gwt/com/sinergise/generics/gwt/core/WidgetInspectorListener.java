package com.sinergise.generics.gwt.core;
import java.util.ArrayList;
import java.util.Map;

import com.google.gwt.xml.client.Element;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.GenericObjectProperty;


public interface WidgetInspectorListener {
	void inspectionCompleted(Element metadata);
	void inspectionCompleted(EntityType entityType, ArrayList<GenericObjectProperty> sortedProperties, Map<String,String> widgetMetadata);
								
}
