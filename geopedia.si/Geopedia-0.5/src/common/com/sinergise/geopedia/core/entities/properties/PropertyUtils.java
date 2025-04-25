package com.sinergise.geopedia.core.entities.properties;

import java.util.Date;

import com.sinergise.common.util.property.BooleanProperty;
import com.sinergise.common.util.property.DateProperty;
import com.sinergise.common.util.property.DoubleProperty;
import com.sinergise.common.util.property.LongProperty;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.property.TextProperty;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Field.FieldType;

public class PropertyUtils {
	//TODO: implement defaults
	public static Property<?> forField(Field field) {
		return forField(field.getType());
	}
	
	public static Property<?> forField(FieldType type) {
		switch (type) {
		case BLOB:
			return new BinaryFileProperty();
		case FOREIGN_ID:
			return new ForeignReferenceProperty();
		case INTEGER:
			return new LongProperty();
		case LONGPLAINTEXT:
		case PLAINTEXT:		
		case STYLE:
			return new TextProperty();
		case BOOLEAN:
			return new BooleanProperty();
		case DATE:
		case DATETIME:
			return new DateProperty();
		case WIKITEXT:
			return new HTMLProperty();
		case DECIMAL:
			return new DoubleProperty();
		default:
			throw new IllegalStateException();
		}
	}

	public static boolean isNull(Property<?> property) {
		if(!(property instanceof HTMLProperty)){
			return property==null || property.getValue()==null;
		} else {
			return StringUtil.isNullOrEmpty(((HTMLProperty)property).getRawHtml());
		}
	}
	

	public enum ValueHolderTypes {INT,LONG,BOOL,STRING,STYLE,WIKI, BINARY, DECIMAL}

	public static Property<?> fromStringValue(String value, int typeId) {
		FieldType type = FieldType.forId(typeId);
		Property<?> prop = forField(type);
		switch (type) {
		case BLOB:
			((BinaryFileProperty)prop).setValue(Long.valueOf(value));
			break;
		case FOREIGN_ID:
			((ForeignReferenceProperty)prop).setValue(Long.valueOf(value));
			break;
		case INTEGER:
			((LongProperty)prop).setValue(Long.valueOf(value));
			break;
		case LONGPLAINTEXT:
		case PLAINTEXT:		
		case STYLE:
			((TextProperty)prop).setValue(value);
			break;
		case BOOLEAN:
			((BooleanProperty)prop).setValue(Boolean.valueOf(value));
			break;
		case DATE:
		case DATETIME:
			((DateProperty)prop).setValue(new Date(Long.parseLong(value)));
			break;
		case WIKITEXT:
			((HTMLProperty)prop).setValue(value);
			break;
		case DECIMAL:
			((DoubleProperty)prop).setValue(Double.valueOf(value));
			break;
		}
		return prop;
	}

	

	public static String propertyToString(Property<?> vh) {
		if (isNull(vh)) return null;
		if (vh instanceof DateProperty) {
			return String.valueOf(((DateProperty)vh).getValue().getTime());
		}
		return vh.toString();
	}
	
	
}
