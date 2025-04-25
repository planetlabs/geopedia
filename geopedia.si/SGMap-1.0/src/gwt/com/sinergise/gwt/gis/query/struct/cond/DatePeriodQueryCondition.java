package com.sinergise.gwt.gis.query.struct.cond;

import static com.sinergise.common.gis.filter.FilterCapabilities.SCALAR_OP_COMP_GREATERTHAN_EQUALTO;
import static com.sinergise.common.gis.filter.FilterCapabilities.SCALAR_OP_COMP_LESSTHAN_EQUALTO;
import static com.sinergise.common.gis.filter.FilterCapabilities.SCALAR_OP_LOGICAL_AND;
import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.sinergise.common.gis.filter.ComparisonOperation;
import com.sinergise.common.gis.filter.ExpressionDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;
import com.sinergise.common.gis.filter.Literal;
import com.sinergise.common.gis.filter.LogicalOperation;
import com.sinergise.common.gis.filter.PropertyName;
import com.sinergise.common.util.format.DateFormatUtil;
import com.sinergise.common.util.lang.Pair;
import com.sinergise.common.util.property.DateProperty;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;

public class DatePeriodQueryCondition extends AbstractQueryCondition {
	
	public static final String QUERY_MODE = "from-to";
	
	public static final FilterCapabilities MINIMAL_FILTER_CAPS 
		= new FilterCapabilities(SCALAR_OP_COMP_LESSTHAN_EQUALTO | SCALAR_OP_COMP_GREATERTHAN_EQUALTO);
	
	private static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat(DateFormatUtil.ISO_DATE_PATTERN);
	private static final String DATE_SEPARATOR = "<>";
	
	private static final String SPECIAL_VALUE_CURRENT_YEAR = "CURRENT_YEAR";

	public DatePeriodQueryCondition(PropertyDescriptor<?> propertyDesc, FilterCapabilities filterCaps) {
		super(propertyDesc, filterCaps);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void setValue(String value, boolean fireEvents) {
		//handle special values
		if (SPECIAL_VALUE_CURRENT_YEAR.equalsIgnoreCase(value)) {
			Date today = new Date();
			value = toStringValue(new Date(today.getYear(), 0, 1), new Date(today.getYear(), 11, 31));
		}
		
		if (parseStringValue(value) == null) value = null; //set to null if not pair
		
		super.setValue(value, fireEvents);
	}
	
	@Override
	public FilterDescriptor getQueryCondition() throws InvalidFilterDescriptorException {
		Pair<Date, Date> pairVal = parseStringValue(getValue());
		if (pairVal == null) return null;
		
		ComparisonOperation fromOp = createCompOp(pairVal.getFirst(), SCALAR_OP_COMP_GREATERTHAN_EQUALTO);
		ComparisonOperation toOp = createCompOp(pairVal.getSecond(), SCALAR_OP_COMP_LESSTHAN_EQUALTO);
		
		if (fromOp != null && toOp != null) {
			return new LogicalOperation(new ExpressionDescriptor[]{fromOp, toOp}, SCALAR_OP_LOGICAL_AND);
		} else if (fromOp != null) {
			return fromOp;
		} else if (toOp != null) {
			return toOp;
		}
		return null;
	}
	
	private ComparisonOperation createCompOp(Date dateVal, int op) throws InvalidFilterDescriptorException {
		if (dateVal == null) return null;
		return new ComparisonOperation(
			new PropertyName(propDesc.getSystemName()), 
			op, 
			Literal.newInstance(new DateProperty(dateVal)));
	}

	@Override
	protected FilterCapabilities getMinimalFilterCaps() {
		return MINIMAL_FILTER_CAPS;
	}
	
	public Pair<Date, Date> getPairValue() {
		return parseStringValue(getValue());
	}
	
	public static Pair<Date, Date> parseStringValue(String strValue) {
		Date fromDate = null;
		Date toDate = null;
		
		if (!isNullOrEmpty(strValue) && strValue.indexOf(DATE_SEPARATOR) > -1) {
			String[] vals = strValue.split(DATE_SEPARATOR);
			if (vals.length > 0 && !isNullOrEmpty(vals[0])) {
				try {
					fromDate = DATE_FORMAT.parse(vals[0]);
				} catch (Exception ignore) {}
			}
			if (vals.length > 1 && !isNullOrEmpty(vals[1])) {
				try {
					toDate = DATE_FORMAT.parse(vals[1]);
				} catch (Exception ignore) {}
			}
			
			return Pair.newPair(fromDate, toDate);
		}
		return null;
	}
	
	public static String toStringValue(Date fromDate, Date toDate) {
		if (fromDate == null && toDate == null) return null;
		
		String ret = "";
		if (fromDate != null) ret += DATE_FORMAT.format(fromDate);
		ret += DATE_SEPARATOR;
		if (toDate != null) ret += DATE_FORMAT.format(toDate);
		return ret;
	}

}
