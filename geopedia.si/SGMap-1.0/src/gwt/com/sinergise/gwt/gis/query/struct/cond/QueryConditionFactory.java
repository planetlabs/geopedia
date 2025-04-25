package com.sinergise.gwt.gis.query.struct.cond;

import static com.sinergise.common.util.property.descriptor.PropertyDescriptor.KEY_QUERY_MODE;
import static com.sinergise.common.util.property.descriptor.PropertyType.VALUE_TYPE_DATE;
import static com.sinergise.common.util.property.descriptor.PropertyType.VALUE_TYPE_LONG;
import static com.sinergise.common.util.property.descriptor.PropertyType.VALUE_TYPE_REAL;

import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;

/**
 * Factory for query conditions. 
 * 
 * @author tcerovski
 */
public class QueryConditionFactory {

	/**
	 * Creates a QueryCondition holder from provided {@link PropertyDescriptor} and {@link FilterCapabilities}.
	 */
	public HasQueryCondition createQueryCondition(PropertyDescriptor<?> pd, FilterCapabilities fc) {
		
		if (pd.getType().getValueType().equals(VALUE_TYPE_DATE)) 
		{
			if (DatePeriodQueryCondition.QUERY_MODE.equalsIgnoreCase(pd.getInfoString(KEY_QUERY_MODE, null))) {
				return new DatePeriodQueryCondition(pd, fc);
			}
			return new DateQueryCondition(pd, fc);
		} else if (pd.getType().getValueType().equals(VALUE_TYPE_REAL)
			|| pd.getType().getValueType().equals(VALUE_TYPE_LONG))
		{
			return new NumberQueryCondition(pd, fc);
		}
		
		return new TextQueryCondition(pd, fc);
	}
	
}
