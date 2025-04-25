package com.sinergise.generics.core.util;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.Types;
import com.sinergise.generics.core.filter.SimpleFilter;

public abstract class FilterProcessorBase {

	protected abstract boolean matchesString(String filterValue, String valueToMatch);
	protected abstract String getMetaAttribute(String entityAttributeName, String metaAttributeName);
	
	private static final org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(FilterProcessorBase.class); 

	
	public  boolean matchesFilter(SimpleFilter filter, EntityObject object) {
		if (filter==null) return true;
		boolean matches = true;
		EntityObject filterData = filter.getFilterData();
		if (filterData==null) return true;
		
		EntityType et = filterData.getType();
		
		for (TypeAttribute ta:et.getAttributes()) {
			String attrFilterValue = filter.getFilterData().getPrimitiveValue(ta.getId());
			if (attrFilterValue==null || attrFilterValue.length()==0) {// skip empty
				logger.trace("Skipping empty filter attribute: {}",ta.getName());
				continue;
			}
			
			String attrValue = object.getPrimitiveValue(ta.getId());
			if (attrValue == null) {
				logger.trace("Filter attribute {} value is '{}', but object os null. Matching failed!",ta.getName(), attrFilterValue);
				return false;
			}
			
			if (ta.getPrimitiveType() == Types.DATE) {
				String dateFormat = getMetaAttribute(ta.getName(), MetaAttributes.VALUE_FORMAT);
				if (dateFormat==null) {
					dateFormat = MetaAttributes.DEFAULT_DATE_FORMAT;
				}
				if (dateFormat!=null && dateFormat.length()>0) {
					DateTimeFormat dtf=DateTimeFormat.getFormat(dateFormat);
					attrValue=dtf.format(new Date(Long.parseLong(attrValue)));
				}
			}
			
			if (!matchesString(attrFilterValue, attrValue)) {
				logger.trace("String match '{}'='{}' failed!",attrFilterValue, attrValue);

				matches=false;
				break;
			}
		}
		return matches;
	}		
}
