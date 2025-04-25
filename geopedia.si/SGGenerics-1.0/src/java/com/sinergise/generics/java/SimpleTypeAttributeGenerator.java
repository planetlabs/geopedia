package com.sinergise.generics.java;

import java.util.Date;

import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.Types;

public class SimpleTypeAttributeGenerator {
	
	@SuppressWarnings("unchecked")
	public static TypeAttribute getAttribute(String name, Class type, boolean isArray) {
		int attributeType = Integer.MIN_VALUE;
		if (type.isAssignableFrom(int.class) || 
				type.isAssignableFrom(Integer.class) ||
				type.isAssignableFrom(short.class) ||
				type.isAssignableFrom(Short.class) ||
				type.isAssignableFrom(long.class) ||
				type.isAssignableFrom(Long.class)) {
				attributeType = Types.INT;
		} else if (type.isAssignableFrom(double.class) ||
				type.isAssignableFrom(float.class) ||
				type.isAssignableFrom(Double.class) ||
				type.isAssignableFrom(Float.class)) {
				attributeType = Types.FLOAT;
		} else if (type.isAssignableFrom(Date.class)) {
				attributeType = Types.DATE;
		} else if (type.isAssignableFrom(boolean.class) ||
				type.isAssignableFrom(Boolean.class)) {
				attributeType = Types.BOOLEAN;
		} else if (type.isAssignableFrom(String.class)) {
				attributeType = Types.STRING;
		}
		if (attributeType!=Integer.MIN_VALUE)
			return new TypeAttribute(Integer.MIN_VALUE, name, attributeType, true, isArray);
		else  {
			EntityType entityType = AbstractEntityTypeStorage.getInstance().getEntityType(type.getName());
			if (entityType!=null) {
				return  new TypeAttribute(Integer.MIN_VALUE, name, entityType.getId(), false, isArray);
			}

		}
		
		throw new RuntimeException("Unable to create TypeAttribute for class '"+type.toString()+"'");
	}
	
	
	public static void addTypeAttribute (EntityType et, String name, Class type, int id) {
		addTypeAttribute(et, name, type, false, id);
	}
	
	public static void addTypeAttribute (EntityType et, String name, Class type, boolean isArray, int id) {
		TypeAttribute ta = getAttribute(name, type, isArray);
		ta.setId(id);
		et.addTypeAttribute(ta);
	}
	



}
