package com.sinergise.generics.gwt.widgetprocessors;

import java.util.ArrayList;
import java.util.Map;

import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.gwt.widgetbuilders.MutableWidgetBuilder;

public class WidgetScriptingHelpers {

	private static final org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(WidgetScriptingHelpers.class); 
	

	/**
	 * Returns true if widget containing named attribute contains non null value
	 * @param wb
	 * @param widget
	 * @param gProperty
	 * @return
	 */
	public static boolean widgethasValue(MutableWidgetBuilder wb, Widget widget, GenericObjectProperty gProperty) {
		String value = (String) wb.getWidgetValue(widget, gProperty.getName(), gProperty.getAttributes());
		
		if (value==null)
			return false;
		return true;
	}
	
	
	private static GenericObjectProperty getPropertyByName(Map<String,GenericObjectProperty> propertyMap, String name) {
		GenericObjectProperty gProperty = propertyMap.get(name);

		if (gProperty==null) {
			logger.error("Unable to find TypeAttribute with name '"+name+"'");
			throw new RuntimeException("Illegal TypeAttribute name '"+name+"'");
		}
		return gProperty;
	}
	/**
	 * Returns true if widget containing named attribute contains non null value
	 * 
	 * @param wb
	 * @param propertyMap
	 * @param widgetMap
	 * @param name
	 * @return
	 */
	public static boolean widgetHasValue(MutableWidgetBuilder wb, Map<String,GenericObjectProperty> propertyMap,  Map<String,Widget> widgetMap, String name) {
		Widget widget = widgetMap.get(name);
		GenericObjectProperty gProperty = getPropertyByName(propertyMap, name);
		
		if (widget==null) {
			logger.error("Unable to find widget for TypeAttribute with name '"+name+"'");
			throw new RuntimeException("Illegal TypeAttribute name '"+name+"'");
		}
		return widgethasValue(wb, widget, gProperty);
		
	}
	
	public static boolean widgetHasValues(MutableWidgetBuilder wb, Map<String,GenericObjectProperty> propertyMap,  Map<String,Widget> widgetMap, ArrayList<String> names) {
		for (String name:names) {
			if (!widgetHasValue(wb,propertyMap,widgetMap,name))
				return false;
		}
		return true;
	}
	
	
	
	public static void setRequiredAttribute(Map<String,GenericObjectProperty> propertyMap, String name, boolean required) {
		GenericObjectProperty gProperty = getPropertyByName(propertyMap, name);
		MetaAttributes.setBooleanAttribute(gProperty.getAttributes(), MetaAttributes.REQUIRED, required);
	}

	
	public static void setRequiredAttributes(Map<String,GenericObjectProperty> propertyMap, String[] names, boolean [] required) {
		if (names.length!=required.length)
			throw new RuntimeException("Attribute names and requirement counts must match!");
		for (int i=0;i<names.length;i++) {
			setRequiredAttribute(propertyMap, names[i], required[i]);
		}
	}
	
	/**
	 * Sets <code>MetaAttributes.REQUIRED</code> attribute for all attributes in names array to required, others to !required.
	 * 
	 * @param propertyMap
	 * @param names
	 * @param required
	 */
	public static void toggleRequiredAttributes(Map<String,GenericObjectProperty> propertyMap, String[] names, boolean required) {
		for (GenericObjectProperty prop:propertyMap.values()) {
			MetaAttributes.setBooleanAttribute(prop.getAttributes(), MetaAttributes.REQUIRED, !required);
		}
		for (String name:names) {
			setRequiredAttribute(propertyMap, name, required);
		}
	}
}
