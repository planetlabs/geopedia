package com.sinergise.generics.core;

public class XMLTags {
	public static final String Entity ="Entity";
	public static final String EntityAttribute = "EntityAttribute";
	public static final String EntityAttributeDefaults = "EntityAttributeDefaults";
	public static final String Action = "Action";
	public static final String Stub = "Stub";
	
	public static final String DefinedEntities = "DefinedEntities";
	public static final String Datasources = "EntityDatasources";
	public static final String XMLEntities ="XMLEntities";
	public static final String DBEntities="DBEntities";
	public static final String Widgets = "Widgets";
	public static final String Filters = "Filters";
	public static final String Includables = "Includables";
	public static final String WidgetMason="WidgetMason";

	public static boolean isSupportTag(String nodeName) {
		if (EntityAttribute.equals(nodeName) ||
			Action.equals(nodeName) ||
			Stub.equals(nodeName))
			return false;
		return true;
	}
	
	
	
	public interface MainConfiguration {
		public static final String TAG_MAIN="main";
		public static final String TAG_INCLUDABLES="includables";
		public static final String TAG_LANGUAGE="language";
		
		public static final String ATTR_SOURCE = "source";
		public static final String ATTR_NAME = "name";
		public static final String ATTR_LANGUAGE = "language";
	}
}
