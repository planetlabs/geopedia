package com.sinergise.generics.core;

import java.util.Map;


public class MetaAttributes {
	
	private static final org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(MetaAttributes.class); 

	
	public static final String ENTITY_ATTRIBUTE_ROWNUM="__ROWNUM__";
	/**
	 *  Internal attribute, DO NOT USE IN XML!
	 */
	public static final String ELEMENT_TYPE="ElementType";
	/**
	 * Attribute name
	 */
	public static final String NAME="name";
	
	/**
	 * Attribute type (one of com.sinergise.generics.core.Types) 
	 * or
	 * Action type (one of .. TODO!)
	 */
	public static final String TYPE="type";
	
	/**
	 * You can add the same element style to many entities/stubs.
	 */
	public static final String EL_STYLE="elStyle";
	/**
	 * Attribute type (SQL)
	 */
	public static final String SQL_TYPE="sqlType";
	/**
	 * Completely ignore this attribute. It is used to override automatically generated
	 * attributes.
	 */
	public static final String IGNORE = "ignore";
	/**
	 * Ignore this attribute when communicating with the database.
	 * full - full ignore (reading and writing)  "true" also equals full ignore (legacy support)
	 * readonly - allow attribute to be read but not written
	 * writeonly - allow attribute to be written but not read
	 */
	public static final String DBIGNORE = "dbIgnore";
	
	public static final String VAL_DBIGNORE_FULL="full";
	public static final String VAL_DBIGNORE_RONLY="readonly";
	public static final String VAL_DBIGNORE_WONLY="writeonly";
	
	
	/**
	 * Searching will ignore case by default. Set this attribute to false
	 * to override this behavior.
	 */
	public static final String SEARCH_IGNORE_CASE="searchIgnoreCase";
	
	/**
	 * Position index when rendering widget. Attributes with position indexes 
	 * should be rendered first, the ones without indexes are added afterwards
	 */
	public static final String POSITION ="position";
	/**
	 * tab index. If ommited, position will be used for tabindex.
	 */
	public static final String TABINDEX = "tabIndex";

	/**
	 * Importance index when rendering widget. Attributes with lower importance indexes should be added first, the ones without indexes are added afterwards. <br>
	 * This is used in when table width is limited. In this case the less important attributes will hide automatically.
	 */
	public static final String IMPORTANCE = "importance";
	/**
	 * Widget label
	 */
	public static final String LABEL = "label";
	/**
	 * Attribute is read-only
	 */
	public static final String READONLY = "readonly";
	/**
	 * Attribute is not displayed
	 */
	public static final String HIDDEN ="hidden";
	/**
	 *  Attribute is required
	 */
	public static final String REQUIRED="required";
	/**
	 * Widget is disabled
	 */
	public static final String DISABLED="disabled";
	
	
	
	
	/*  VALUE FORMATTING */
	
	/**
	 * Value formatter (depends on attribute type)
	 */
	public static final String VALUE_FORMAT="valueFormat";
	/**
	 * minimum value
	 */
	public static final String VALUE_MINIMUM = "valueMinimum";
	/**
	 * maximum value
	 */
	public static final String VALUE_MAXIMUM = "valueMaximum";			
	/**
	 * auto correct range (min/max) value
	 */
	public static final String DISABLE_VALUE_RANGE_AUTOCORRECT="disableValueAutoCorrect";
	
	
	/**
	 * value length.
	 * For character and date types it contains maximum possible characters
	 * For numeric types it represents the precision
	 */
	public static final String VALUE_LENGTH = "valueLength";
	
	
	/* VALUE CONTENT */
	/**
	 *  widget hint 
	 */
	public static final String WIDGET_VALUE_HINT="valueHint";
	/** 
	 * Default value provider. 
	 * Provides default value according to implementation used. See <code>com.sinergise.generics.gwt.widgetprocessors.helpers.DefaultValueProvider</code>
	 * implementations for value providers.
	 */
	public static final String DEFAULT_VALUE_DESCRIPTOR = "defaultValueDescriptor";
	
	
	
	/*
	 * Attribute is primary key
	 */
	public static final String PRIMARYKEY ="primarykey";
	
	public static final String DATASOURCEID = "datasourceid";
	
	public static final String TABLE_MAXROWS = "tableMaxRows";
	
	public static final String TABLE_FILTERROW = "hasFilterRow";
	
	
	
		
	/*
	 * Widget rendering modificaations 
	 */
	
	/**
	 * Use radio button widget for rendering
	 */
	public static final String RENDER_AS_RADIOBUTTON = "asRadio";
	/**
	 * Use label widget for rendering
	 */
	public static final String RENDER_AS_LABEL = "asLabel";
	/**
	 * Use anchor widget for rendering
	 */
	public static final String RENDER_AS_ANCHOR = "asAnchor";
	/**
	 * Use spinner widget for rendering
	 */
	public static final String RENDER_AS_SPINNER="asSpinner";
	/**
	 * Render as large widget (TextArea)
	 */
	public static final String LARGE = "large";
	/**
	 * Render as Lookup (ComboBox, Complex lookup,...)
	 * @see LOOKUP_* MetaAttributes for detailed definition of lookup behavior
	 */
	public static final String LOOKUP="lookup";

	
	
	public static final String FILE_UPLOAD_COUNT = "fileUploadCount";
	
	/* Lookup related meta attributes
	 */
	
	/**
	 * DatasourceID for datasource based lookups
	 */
	public static final String LOOKUP_SOURCE = "lookupSource";
	
	public static final String RENDER_AS_ROW_BTN = "renderAsRowButton";
	/**
	 * Lookup label(s)
	 * For simple lookup: comma separated list of lookup labels 
	 * For datasource based lookups: name of the datasource EntityAttribute that containes lookup values
	 */
	public static final String LOOKUP_LABELS = "lookupLabels";
	
	/**
	 * Formatting of lookup labels
	 */
	public static final String LOOKUP_LABELS_FORMAT="lookupLabelsFormat";
	/**
	 * Lookup key(s)
	 * For simple lookup: comma separated list of lookup keys 
	 * For datasource based lookups: name of the datasource EntityAttribute that containes key values
	 */	
	public static final String LOOKUP_KEYS="lookupKeys";
	/**
	 * Adds empty choice (key=null, label=blank) in the lookup widget. 
	 */
	public static final String LOOKUP_HAS_EMPTY_CHOICE = "lookupHasEmptyChoice";
	/**
	 * Contains EntityType of the entity that will be used to look-up desired value.
	 * Used when constructing complex lookups with <code>LookupGenericWidget</code>. 
	 * 
	 */
	public static final String LOOKUP_ENTITYTYPE="lookupEntityType";
	/**
	 * Widget name of the widget, that will be used to construct the lookup.
	 * Used when constructing complex lookups with <code>LookupGenericWidget</code>. 
	 */
	public static final String LOOKUP_WIDGET="lookupWidget";	
	
	/**
	 * lookup data is cached
	 */
	public static final String LOOKUP_CACHED="lookupDataCached";
	
	
	public static final String LOOKUP_FILTER="lookupFilter";
	
	/**
	 * Select only distinct keys
	 */
	public static final String LOOKUP_DISTINCTKEYS="lookupDistinctKeys";

	/**
	 * Allow multiple selection
	 */
	public static final String LOOKUP_MULTISELECT="lookupMultiselect";
	
	/**
	 * filter on csv field
	 */
	public static final String FILTER_CSV="filterCSV";
	public static final String FILTER_CSV_SEPARATOR="filterCSVSeparator";
	
	public static final String EXPORT_TO_FILE="exportToFile";
	
	/**
	 * filter on date field
	 */
	public static final String FILTER_DATE="filterDate";
	

	
	/*** Widget meta attributes ***/
	
	public static final String WIDGET_ATTRIBUTE_IGNORE_LIST="wgAttributeIgnoreList";
	
	
	/***TO BE REMOVED!***/
	
	/** styling **/
	@Deprecated
	public static final String STYLE_BASE="styleBase";
	
	
	/** Defaults 
	 * !!!Defaults are not used as meta attributes!!
	 * TODO: move to separate class?
	 * */
	
	/***
	 * Default date format
	 */
	public static final String DEFAULT_DATE_FORMAT="yyyy-MM-dd";
	public static final String BOOLEAN_TRUE="true";
	public static final String BOOLEAN_FALSE="false";
	

	/**
	 * 
	 */

	public static final String META_DATE_FORMAT="date";
	public static final String META_TIME_FORMAT="time";
	public static final String META_DATETIME_FORMAT="datetime";
	
	public static final String RENDER_AS_FACTOR_MULTIPLIER = "renderAsFactorMultiplier";
	public static final String FACTOR_MULTIPLIER = "factorMultiplier";
	public static final String FACTOR_MULTIPLIER_2 = "factorMultiplier2";

	/**
	 * Default value attribute specifies the initial value of the field
	 */	
    public static final String DEFAULT_VALUE = "defaultValue";
	
	public static final String META_INCLUDE ="include";
	
	public static final String REGEX_DATETIME_FORMAT = "("+MetaAttributes.META_DATETIME_FORMAT+"|"
	+ MetaAttributes.META_DATE_FORMAT+"|"
	+ MetaAttributes.META_TIME_FORMAT+")=(short|medium|long)";
	
	/**
	 * 
	 * @param attributes
	 * @param metaAttribute
	 * @return
	 */
	
	public static boolean isTrue(Map<String,String> attributes, String metaAttribute) {
		if (attributes == null || attributes.size()==0)
			return false;
		if (BOOLEAN_TRUE.equalsIgnoreCase(attributes.get(metaAttribute)))
			return true;
		return false;
	}
	public static boolean isTrue(String value) {
		if (value==null || !BOOLEAN_TRUE.equalsIgnoreCase(value))
			return false;
		return true;
	}
	
	public static boolean isFalse(Map<String,String> attributes, String metaAttribute) {
		if (attributes == null || attributes.size()==0) return true;
		String value = attributes.get(metaAttribute);
		if (value == null || BOOLEAN_FALSE.equalsIgnoreCase(value)) return true;
		return false;
	}
	public static boolean isFalse(String value) {
		if (value==null || BOOLEAN_FALSE.equalsIgnoreCase(value) || value.length()==0) {
			return true;
		}
		return false;
	}
	
	
	public static boolean hasAttribute(Map<String,String> attributes, String metaAttribute) {
		if (attributes == null || metaAttribute == null || metaAttribute.length()==0)
			return false;	
		String attribute = attributes.get(metaAttribute);
		if (attribute!=null && attribute.length()>0)
			return true;
		return false;	
	}
	
	public static boolean isType(Map<String,String> attributes, int type) {
		if (!attributes.containsKey(TYPE))
			return false;
		if (attributes.get(TYPE).equals(Integer.toString(type)))
			return true;
		return false;
	}

	public static String readRequiredStringAttribute (Map<String,String> attributes, String attribute) {
		if (attribute == null || attribute.length()==0) {
			logger.error("Error while trying to read a required String attribute. Attribute may not be null!");
			throw new RuntimeException("Attribute may not be null or empty");
		}
		String strVal = attributes.get(attribute);
		if (strVal==null || strVal.length()==0) {
			logger.error("Unable to read required string attribute '"+attribute+"' (name='"+attributes.get(NAME)+"'");
			throw new RuntimeException("Unable to read required attribute '"+attribute+"'");
		}
		return strVal;
	}
	
	public static String readStringAttr(Map<String,String> attributes, String attribute, String defaultValue) {
		if (attributes==null || attributes.size()==0 || attribute==null || attribute.length()==0)
			return defaultValue;
		String strVal = attributes.get(attribute);
		if (strVal==null)
			return defaultValue;
		return strVal;
	}
	
	public static boolean readBoolAttr(Map<String,String> attributes, String attribute, boolean defaultValue) {
		if (attributes==null || attributes.size()==0 || attribute==null || attribute.length()==0)
			return defaultValue;
		String strVal = attributes.get(attribute);
		if (strVal==null)
			return defaultValue;
		return isTrue(strVal);
	}

	public static Double readDoubleAttr(Map<String,String> attributes, String attribute, Double defaultValue) {
		if (attributes==null || attributes.size()==0 || attribute==null || attribute.length()==0)
			return defaultValue;
		String strVal = attributes.get(attribute);
		if (strVal==null)
			return defaultValue;
		try {
			return Double.valueOf(strVal);
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Attribute value: "+attribute+"='"+strVal+"' is not a double!");
		}
	}
	
	public static int readIntAttr(Map<String,String> attributes, String attribute, int defaultValue) {
		if (attributes==null || attributes.size()==0 || attribute==null || attribute.length()==0)
			return defaultValue;
		String strVal = attributes.get(attribute);
		if (strVal==null)
			return defaultValue;
		try {
			return Integer.parseInt(strVal);
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Attribute value: "+attribute+"='"+strVal+"' is not an integer!");
		}
	}
	
	public static boolean isAttribute(Map<String,String> attributes, String attributeName, String value) {
		if (attributes == null || attributes.size()==0)
			return false;
		if (value.equals(attributes.get(attributeName)))
			return true;
		return false;
	}
	
	public static void setBooleanAttribute(Map<String, String> attributes,
			String attributeName, boolean required) {
		if (required) {
			attributes.put(attributeName, BOOLEAN_TRUE);
		} else {
			attributes.put(attributeName, BOOLEAN_FALSE);
		}
	}

	public static final String META_MAXCOLUMNS = "FlexTableWidget-MaxColumns";

	public static final String META_NEXTROW = "FlexTableWidget-NextRow";

	public static final String META_COLSPAN="FlexTableWidget-Colspan";

	/**
	 * Add style name to the current row. This way you have control over label and value cell.
	 */
	public static final String META_ROW_STYLE="Style";

	public static final String META_GROUP="FlexTableWidget-group";

	public static final String META_GROUPLABEL="FlexTableWidget-groupLabel";

	public static final String WIDGET_FLEXTABLE="FlexTable";

	public static final String WIDGET_DIV="Div";
	
	public static final String WIDGET_DIV_SHOWLABLES="showLables";

	public static final String STUB_VALUE = "stubValue";
		
}
