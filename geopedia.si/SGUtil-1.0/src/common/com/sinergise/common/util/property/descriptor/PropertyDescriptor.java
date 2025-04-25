/*
 *
 */
package com.sinergise.common.util.property.descriptor;

import java.util.Comparator;
import java.util.Date;

import com.sinergise.common.util.auxprops.AbstractAuxiliaryInfo;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.common.util.property.Property;

public class PropertyDescriptor<T> extends AbstractAuxiliaryInfo {
	
	private static final long serialVersionUID = 1L;
	
	public static final String KEY_VALUE_EXPR = "valueExpression";
    public static final String KEY_HIDDEN = "hidden";
    public static final String KEY_EXPORTABLE = "exportable";
    public static final String KEY_READONLY = "readOnly";
    public static final String KEY_SHOW_AS_LINK = "showAsLink";
	public static final String KEY_IMPORTANCE	= "importance";
	public static final String KEY_ORDER = "order";
	public static final String KEY_INDEX_IN_FEATURE = "indexInFeature";
	public static final String KEY_LINK_URL_EXPR = "PropertyLinkURLExpression";
	public static final String KEY_TOOLTIP_EXPR = "tooltipExpr";
	public static final String KEY_QUERYABLE = "queryable"; //default: true
	public static final String KEY_QUERY_MODE = "queryMode";
	public static final String KEY_QUERY_REQUIRED = "queryRequired"; //default: false
	
	public static final String KEY_DB_FIELD_NAME = "dbFieldName"; //default: @a systemName
	
	public static final String KEY_FORCE_WILDCARD = "forceWildcard"; //default: true
	public static final String KEY_CASE_INSENSITIVE = "caseInsensitive"; //default: true
	public static final String KEY_UPPERCASED = "uppercased"; 
	public static final String KEY_LOOKUP = "lookup"; //default: false
	public static final String KEY_LOOKUP_TYPE = "lookupType";
	public static final String KEY_IGNORE = "ignore";
    public static final String KEY_PERSISTENT = "persistent";
    
	public static final String KEY_CONDITIONALLY_MANDATORY = "conditionallyMandatory";
	
	protected PropertyType<T> type;
    protected String systemName;
    protected PropertyDescriptor<? super T> defaults;
    
    /**
     * @deprecated serialization only
     */
    @Deprecated
    public PropertyDescriptor() {
    	// don't use
	}
    
    public PropertyDescriptor(PropertyDescriptor<? super T> defaults, String systemName, PropertyType<T> type) {
    	this.systemName = systemName;
    	this.defaults = defaults;
    	this.type=type;
    	setTitle(systemName);
    	setDescription(systemName);
	}
    
    @SuppressWarnings({"unchecked", "rawtypes"})
	public PropertyDescriptor(String systemName, PropertyType<T> type) {
    	this((PropertyDescriptor)null, systemName, type);
	}

    public String getSystemName() {
    	return systemName;
    }
    
    @Override
    public String getTitle() {
    	String ret = super.getTitle();
    	return ret != null ? ret : getSystemName();
    }
    
    public void setReadOnly(boolean readOnly){
    	setInfoBoolean(KEY_READONLY, readOnly);
    }
    
    public boolean isReadOnly() {
    	return data.getBoolean(KEY_READONLY, defaults == null ? false : defaults.isReadOnly());
    }
    
    public boolean isHidden() {
    	return data.getBoolean(KEY_HIDDEN, defaults==null ? false : defaults.isHidden());
    }
    
    public boolean isExportable() {
    	return data.getBoolean(KEY_EXPORTABLE, defaults==null ? false : defaults.isExportable());
    }
    
    /** Shouldn't be fetched if <code>true</code>. <code>false</code> by default. */
    public boolean ignore() {
    	return data.getBoolean(KEY_IGNORE, defaults==null ? false : defaults.ignore());
    }
    
    /** true by default */
    public boolean isQueryable() {
    	return data.getBoolean(KEY_QUERYABLE, defaults==null ? true : defaults.isQueryable());
    }
    
    /** false by default */
    public boolean isQueryRequired() {
    	return data.getBoolean(KEY_QUERY_REQUIRED, defaults==null ? false : defaults.isQueryRequired());
    }
    
    /** true by default */
    public boolean isForceWildcard() {
    	return data.getBoolean(KEY_FORCE_WILDCARD, true);
    }
    
    /** true by default */
    public boolean isCaseInsensitive() {
    	return data.getBoolean(KEY_CASE_INSENSITIVE, true);
    }
    
    /** false by default */
    public boolean isLookup() {
    	return data.getBoolean(KEY_LOOKUP, false);
    }
    
    public boolean showAsLink() {
    	return data.getBoolean(KEY_SHOW_AS_LINK, defaults==null ? false : defaults.showAsLink());
    }
    
    public String getLinkUrlExpr() {
    	return data.getString(KEY_LINK_URL_EXPR, defaults==null ? "" : defaults.getLinkUrlExpr());
    }
    
    public String getValueExpr() {
    	return data.getString(KEY_VALUE_EXPR, null);
    }
    
    public String getTooltipExpr() {
    	return data.getString(KEY_TOOLTIP_EXPR, defaults==null ? null : defaults.getTooltipExpr());
    }
    
    public PropertyType<T> getType() {
    	return type;
    }
    
    /**
     * @return order or Integer.MAX_VALUE;
     */
    public int getOrder() {
    	return data.getInt(KEY_ORDER, Integer.MAX_VALUE);
    }

    public int getIndexInFeature() {
    	return data.getInt(KEY_INDEX_IN_FEATURE, Integer.MAX_VALUE);
    }
    
    public int getImportance() {
    	return data.getInt(KEY_IMPORTANCE, Integer.MAX_VALUE);
    }
    
    public static class OrderComparator implements Comparator<PropertyDescriptor<?>> {
    	@Override
		public int compare(PropertyDescriptor<?> o1, PropertyDescriptor<?> o2) {
    		int ret = MathUtil.compare(o1.getOrder(), o2.getOrder());
    		if (ret == 0) {
    			ret = MathUtil.compare(o1.getImportance(),  o2.getImportance());
    			
        		if (ret == 0) {
        			ret = MathUtil.compare(o1.getIndexInFeature(),  o2.getIndexInFeature());
        		}
    		}
    		return ret;
    	}
    }
    
    public static class ImportanceComparator implements Comparator<PropertyDescriptor<?>> {
    	@Override
		public int compare(PropertyDescriptor<?> o1, PropertyDescriptor<?> o2) {
    		int ret = MathUtil.compare(o1.getImportance(),  o2.getImportance());
    		if (ret == 0) {
    			ret = MathUtil.compare(o1.getOrder(), o2.getOrder());
    			
        		if (ret == 0) {
        			ret = MathUtil.compare(o1.getIndexInFeature(),  o2.getIndexInFeature());
        		}
    		}
    		return ret;
    	}
    }

    public String getStringValue(Property<? extends T> property) {
    	return type.format(property);
    }
    
    @Override
    public String toString() {
    	return systemName + " " + type.toString();
    }
    
    public boolean isValueType(PropertyType<?> generic) {
		return type.isType(generic.getValueType());
	}

	public boolean isValueType(String valueType) {
		return type.isType(valueType);
	}
	
	public boolean validate(Property<T> prop) {
		return validate (prop, new PropertyValidationHints());
	}
	
	public boolean validate(Property<T> prop, PropertyValidationHints pvHints) {
		return type.validate(prop, pvHints);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((systemName == null) ? 0 : systemName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PropertyDescriptor<?> other = (PropertyDescriptor<?>)obj;
		if (systemName == null) {
			if (other.systemName != null) {
				return false;
			}
		} else if (!systemName.equals(other.systemName)) {
			return false;
		}
		return true;
	}

	public boolean isPotentiallyMandatory() {
		return isMandatory() || getInfoBoolean(KEY_CONDITIONALLY_MANDATORY, false);
	}
	
	public boolean isMandatory() {
		return getType().isMandatory();
	}
	
	public static PropertyDescriptor<String> createGenericText(String name) {
		return new PropertyDescriptor<String>(name, PropertyType.GENERIC_TEXT);
	}
	public static PropertyDescriptor<Long> createGenericLong(String name) {
		return new PropertyDescriptor<Long>(name, PropertyType.GENERIC_LONG);
	}
	public static PropertyDescriptor<Double> createGenericReal(String name) {
		return new PropertyDescriptor<Double>(name, PropertyType.GENERIC_REAL);
	}
	public static PropertyDescriptor<Date> createDate(String name) {
		PropertyType<Date> dateType = new PropertyType<Date>(PropertyType.VALUE_TYPE_DATE);
		dateType.setInfoInt(PropertyType.KEY_SCALE, PropertyType.DATE_SCALE_DATE);
		return new PropertyDescriptor<Date>(name, dateType);
	}
	public static PropertyDescriptor<Date> createDateTime(String name) {
		PropertyType<Date> dateType = new PropertyType<Date>(PropertyType.VALUE_TYPE_DATE);
		dateType.setInfoInt(PropertyType.KEY_SCALE, PropertyType.DATE_SCALE_DATETIME);
		return new PropertyDescriptor<Date>(name, dateType);
	}
}
