package com.sinergise.generics.core.util;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import com.sinergise.common.util.string.StringUtil;
import com.sinergise.generics.core.AbstractEntityObject;
import com.sinergise.generics.core.ArrayValueHolder;
import com.sinergise.generics.core.BinaryValueHolder;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.LookupPrimitiveValue;
import com.sinergise.generics.core.MasterDetailsHolder;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.PrimitiveValue;
import com.sinergise.generics.core.PrimitiveValueArray;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.Types;
import com.sinergise.generics.core.ValueHolder;

public class EntityUtils {
	
	public static String getStringValue(EntityObject eo, TypeAttribute ta) {
		if (ta==null)
			return null; // TODO? check if primitive
		return eo.getPrimitiveValue(ta.getId());		
	}
	
	public static String getStringValue(EntityObject eo, String attributeName) {
		EntityType et = eo.getType();
		TypeAttribute ta = et.getAttribute(attributeName);
		return getStringValue(eo, ta);
	}
	
	public static String getLookedupValue(EntityObject eo, String attributeName) {
		EntityType et = eo.getType();
		TypeAttribute ta = et.getAttribute(attributeName);
		if (ta==null)
			return null;
		ValueHolder vh = eo.getValue(ta.getId());
		if (vh instanceof LookupPrimitiveValue) {
			return ((LookupPrimitiveValue)vh).lookedUpValue;
		}
		return ((PrimitiveValue)vh).value;

	}

	
	public static Integer getIntegerValue(EntityObject eo, TypeAttribute ta) {
		String value = getStringValue(eo, ta);
		
		if (value == null || value.length() == 0)
			return null;		
		//XXX: Returning null on exception is inconsistent with the other get*Value methods in this class! 
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
		}
		return null;
		
	}
	public static Integer getIntegerValue(EntityObject eo, String attributeName) {
		EntityType et = eo.getType();
		TypeAttribute ta = et.getAttribute(attributeName);
		return getIntegerValue(eo, ta);
	}

	public static Long getLongValue(EntityObject eo, TypeAttribute ta) {
		String value = StringUtil.trimNullEmpty(getStringValue(eo, ta));
		if (value == null) return null;
		return Long.valueOf(value);
	}
	
	public static Long getLongValue(EntityObject eo, String attributeName) {
		EntityType et = eo.getType();
		return getLongValue(eo,  et.getAttribute(attributeName));
	}
	
	public static Double getDoubleValue(EntityObject eo, TypeAttribute ta) {
		if (ta==null) return null;
		String value = StringUtil.trimNullEmpty(getStringValue(eo, ta));
		if (value == null) return null;
		return Double.valueOf(value);

	}
	public static Double getDoubleValue(EntityObject eo, String attributeName) {
		EntityType et = eo.getType();
		return getDoubleValue(eo,  et.getAttribute(attributeName));
	}
	

	public static Boolean getBooleanValue(EntityObject eo, TypeAttribute ta) {
		String value = StringUtil.trimNullEmpty(getStringValue(eo, ta));
		if (value == null) return null;
		return Boolean.valueOf(value);
	}

	public static Boolean getBooleanValue(EntityObject eo, String attributeName) {
		EntityType et = eo.getType();
		return getBooleanValue(eo,  et.getAttribute(attributeName));
	}
	
	public static void setBooleanValue(EntityObject eo, String attributeName, Boolean value) {
		setStringValue(eo, attributeName, value == null ? null : value.booleanValue()
			? MetaAttributes.BOOLEAN_TRUE
			: MetaAttributes.BOOLEAN_FALSE);
	}

	
	public static void setLongValue(EntityObject eo, String attributeName, Long value) {
		setStringValue(eo, attributeName, StringUtil.toString(value));
		
	}
	public static void setIntValue(EntityObject eo, String attributeName, Integer value) {
		setStringValue(eo, attributeName,  StringUtil.toString(value));
	}
	
	public static void setDoubleValue(EntityObject eo, String attributeName, Double value) {
		setStringValue(eo, attributeName,  StringUtil.toString(value));
	}

	public static void setStringValue(EntityObject eo, String attributeName, String value) {
		EntityType et = eo.getType();
		TypeAttribute ta = et.getAttribute(attributeName);
		if (ta==null) return ; // TODO? check if primitive
		eo.setPrimitiveValue(ta.getId(), value);
	}

	public static void setBinaryValue(EntityObject eo,
			String attributeName, byte[] byteArray) {
		EntityType et = eo.getType();
		TypeAttribute ta = et.getAttribute(attributeName);
		if (ta==null) return ; // TODO? check if primitive
		eo.setValue(ta.getId(), new BinaryValueHolder(byteArray));
	}
	
	public static PrimitiveValue getPrimitiveValue (EntityObject eo, String attributeName) {
		EntityType et = eo.getType();
		TypeAttribute ta = et.getAttribute(attributeName);
		if (ta==null || !ta.isPrimitive())
			return null;
		return (PrimitiveValue)eo.getValue(ta.getId());
	}
	
	
	
	
	public static boolean isTrue(EntityObject eo, String attributeName) {
		EntityType et = eo.getType();
		TypeAttribute ta = et.getAttribute(attributeName);
		if (ta == null) throw new RuntimeException("EntityAttribute '"+attributeName+"' does not exist for entity '"+et.getName()+"'");
		if (!ta.isPrimitive()) throw new RuntimeException("EntityAttribute '"+attributeName+"' for entity '"+et.getName()+"' is not a primitive!");
		if (Boolean.TRUE.toString().equals(((PrimitiveValue)eo.getValue(ta.getId())).value)) return true;
		return false;
	}

	
	public static Date getDateValue(EntityObject eo, TypeAttribute ta) {
		String value = StringUtil.trimNullEmpty(getStringValue(eo, ta));
		if (value == null) return null;
		Long time = getLongValue(eo, ta);
		return new Date(time.longValue());
	}

	public static Date getDateValue(EntityObject eo, String attributeName) {
		EntityType et = eo.getType();
		return getDateValue(eo,  et.getAttribute(attributeName));
	}

	public static void setDateValue(EntityObject eo, String attributeName, java.util.Date value) {
		EntityType et = eo.getType();
		TypeAttribute ta = et.getAttribute(attributeName);
		if (ta==null)
			return ; // TODO? check if primitive
		
		String strVal = new Long(value.getTime()).toString();
		eo.setPrimitiveValue(ta.getId(), strVal);
	}

	
	
	public static void setEntityAttributeMetaAttribute(Map<String,Map<String,String>> map, String entityAttribute, String metaAttribute, String metaAttributeValue)  {		
		if (map==null)
			throw new RuntimeException("map attribute must be non-null!");
		Map<String,String> attMap = map.get(entityAttribute);
		if (attMap==null) {
			attMap=new HashMap<String,String>();
			map.put(entityAttribute, attMap);
		}
		
		attMap.put(metaAttribute, metaAttributeValue);
	}

	public static void setNull(EntityObject eo) {
		EntityType et = eo.getType();
		for (TypeAttribute ta:et.getAttributes()) {
			eo.setPrimitiveValue(ta.getId(), null);
		}
	}

	public static ValueHolder deepCopy(ValueHolder vh) {
		if (vh == null) return null;
		if (vh instanceof LookupPrimitiveValue) {
			return internalDeepCopy((LookupPrimitiveValue)vh);
			
		} else if (vh instanceof PrimitiveValue) {
			return internalDeepCopy((PrimitiveValue)vh);
			
		} else if (vh instanceof PrimitiveValueArray) {
			return internalDeepCopy((PrimitiveValueArray)vh);
			
		} else if (vh instanceof ArrayValueHolder) {
			return internalDeepCopy((ArrayValueHolder)vh);
			
		} else if (vh instanceof MasterDetailsHolder) {
			return internalDeepCopy((MasterDetailsHolder)vh);
			
		} else if (vh instanceof AbstractEntityObject) {
			return internalDeepCopy((AbstractEntityObject)vh);
		}
		throw new IllegalArgumentException("Unknown ValueHolder type "+vh);
	}

	private static ValueHolder internalDeepCopy(AbstractEntityObject vh) {
		return new AbstractEntityObject(vh);
	}

	private static ValueHolder internalDeepCopy(MasterDetailsHolder vh) {
		return new MasterDetailsHolder(vh);
	}

	private static ValueHolder internalDeepCopy(ArrayValueHolder vh) {
		ArrayValueHolder ret = new ArrayValueHolder(vh.getType());
		for (ValueHolder item : vh) {
			ret.add(deepCopy(item));
		}
		return ret;
	}

	private static ValueHolder internalDeepCopy(PrimitiveValueArray vh) {
		PrimitiveValueArray ret = new PrimitiveValueArray();
		for (PrimitiveValue pv : vh) {
			ret.add((PrimitiveValue)deepCopy(pv));
		}
		return ret;
	}

	private static PrimitiveValue internalDeepCopy(PrimitiveValue vh) {
		return new PrimitiveValue(vh.value);
	}

	private static LookupPrimitiveValue internalDeepCopy(LookupPrimitiveValue lpv) {
		LookupPrimitiveValue ret = new LookupPrimitiveValue();
		ret.value = lpv.value;
		ret.lookedUpValue = lpv.lookedUpValue;
		return ret;
	}
	
	public static final String styleForPrimitiveType(int type) {
		switch (type) {
			case Types.BOOLEAN: return "genType-boolean";
			case Types.INT: return "genType-int";
			case Types.DATE: return "genType-date";
			case Types.FLOAT: return "genType-float";
			case Types.STRING: return "genType-string";
			case Types.STUB: return "genType-stub";
			case Types.RAW: return "genType-raw";
		}
		return "genType-unknown";
	}

	/**
	 * Clones EntityObject fromEO to EntityObject toEO
	 * TypeAttributes of same name and type are copied. 	
	 * 
	 * @param fromEO
	 * @param toEO
	 */
	public static void cloneEntityObject(EntityObject fromEO, EntityObject toEO) {
		EntityType sourceType = fromEO.getType();
		EntityType destType = toEO.getType();		
		for (TypeAttribute ta:destType.getAttributes()) {
			TypeAttribute srcTA = sourceType.getAttribute(ta.getName());
			if (srcTA!=null && srcTA.getPrimitiveType() == ta.getPrimitiveType()) {
				toEO.setValue(ta.getId(), fromEO.getValue(srcTA.getId()));
			}
		}
	}
	
}
