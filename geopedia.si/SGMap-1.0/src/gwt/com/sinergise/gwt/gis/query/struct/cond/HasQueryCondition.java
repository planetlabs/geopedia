package com.sinergise.gwt.gis.query.struct.cond;

import com.google.gwt.user.client.ui.HasValue;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;

/**
 * Holds a query condition as String value and parses it to ExpressionDescriptor. 
 * 
 * @author tcerovski
 */
public interface HasQueryCondition extends HasValue<String> {

	FilterDescriptor getQueryCondition() throws InvalidFilterDescriptorException;
	
	PropertyDescriptor<?> getPropertyDescriptor();
	
}
