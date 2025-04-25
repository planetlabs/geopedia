package com.sinergise.gwt.gis.query.struct.cond;

import static com.sinergise.common.gis.filter.FilterCapabilities.SCALAR_OP_COMP_EQUALTO;
import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.sinergise.common.gis.filter.ComparisonOperation;
import com.sinergise.common.gis.filter.ElementDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;
import com.sinergise.common.gis.filter.Literal;
import com.sinergise.common.gis.filter.PropertyName;
import com.sinergise.common.util.format.DateFormatUtil;
import com.sinergise.common.util.property.DateProperty;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;

public class DateQueryCondition extends AbstractQueryCondition {
	
	public static final FilterCapabilities MINIMAL_FILTER_CAPS 
		= new FilterCapabilities(SCALAR_OP_COMP_EQUALTO);
	
	private static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat(DateFormatUtil.ISO_DATE_PATTERN);
	
	public DateQueryCondition(PropertyDescriptor<?> propertyDesc, FilterCapabilities filterCaps) {
		super(propertyDesc, filterCaps);
	}
	
	@Override
	public void setValue(String value, boolean fireEvents) {
		if (parseStringValue(value) == null) value = null; //set to null if not date
		super.setValue(value, fireEvents);
	}

	@Override
	public FilterDescriptor getQueryCondition() throws InvalidFilterDescriptorException {
		Date dateVal = parseStringValue(getValue());
		if (dateVal != null) {
			ElementDescriptor left = new PropertyName(propDesc.getSystemName());
			ElementDescriptor right = Literal.newInstance(new DateProperty(dateVal));
			return new ComparisonOperation(left, SCALAR_OP_COMP_EQUALTO, right);
		}
		return null;
	}

	@Override
	protected FilterCapabilities getMinimalFilterCaps() {
		return MINIMAL_FILTER_CAPS;
	}
	
	public Date getDateValue() {
		return parseStringValue(getValue());
	}
	
	public static Date parseStringValue(String strValue) {
		if (!isNullOrEmpty(strValue)) {
			try {
				return DATE_FORMAT.parse(strValue);
			} catch (Exception ignore) {}
		}
		return null;
	}
	
	public static String toStringValue(Date dateValue) {
		if (dateValue == null) return null;
		return DATE_FORMAT.format(dateValue);
	}

}
