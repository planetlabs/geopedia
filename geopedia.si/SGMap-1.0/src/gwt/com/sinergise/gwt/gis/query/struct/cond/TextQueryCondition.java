package com.sinergise.gwt.gis.query.struct.cond;

import static com.sinergise.common.gis.filter.FilterCapabilities.SCALAR_FUNCT_STRING_UPPERCASE;
import static com.sinergise.common.gis.filter.FilterCapabilities.SCALAR_OP_COMP_CONTAINS;
import static com.sinergise.common.gis.filter.FilterCapabilities.SCALAR_OP_COMP_EQUALTO;
import static com.sinergise.common.gis.filter.FilterCapabilities.SCALAR_OP_COMP_LIKE;
import static com.sinergise.common.util.property.descriptor.PropertyDescriptor.KEY_QUERY_MODE;
import static com.sinergise.common.util.property.descriptor.PropertyDescriptor.KEY_UPPERCASED;

import com.sinergise.common.gis.filter.ComparisonOperation;
import com.sinergise.common.gis.filter.ElementDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.Function;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;
import com.sinergise.common.gis.filter.Literal;
import com.sinergise.common.gis.filter.PropertyName;
import com.sinergise.common.util.property.TextProperty;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.common.util.property.descriptor.PropertyType;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.common.util.string.WildcardUtil;
import com.sinergise.common.util.string.WildcardUtil.Wildcards;

public class TextQueryCondition extends AbstractQueryCondition {
	
	public static final FilterCapabilities MINIMAL_FILTER_CAPS = new FilterCapabilities(SCALAR_OP_COMP_EQUALTO);
	
	public static final String QUERY_MODE_CONTAINS = "contains";
	
	protected Wildcards sourceWC = WildcardUtil.WCARD_WINDOWS;

	public TextQueryCondition(PropertyDescriptor<?> propertyDesc, FilterCapabilities filterCaps) {
		super(propertyDesc, filterCaps);
	}
	
	@Override
	public FilterDescriptor getQueryCondition() throws InvalidFilterDescriptorException {
		if (StringUtil.isNullOrEmpty(value)) {
			return null;
		}
		
		String queryVal = value;
		String queryMode = propDesc.getInfoString(KEY_QUERY_MODE, "");
		int operation = SCALAR_OP_COMP_EQUALTO;
		
		if (QUERY_MODE_CONTAINS.equals(queryMode) && filterCaps.supportsFunction(SCALAR_OP_COMP_CONTAINS)) {
			return new ComparisonOperation(new PropertyName(propDesc.getSystemName()),
				SCALAR_OP_COMP_CONTAINS, createContainsQueryLiteral(queryVal)); 
		} 
		
		if(filterCaps.supportsOperation(SCALAR_OP_COMP_LIKE)) {
			operation = SCALAR_OP_COMP_LIKE;
			
			// replace wildcard chars with supported ones
			char wc = WildcardUtil.WCARD_ANSI_LIKE.wString;
			queryVal = WildcardUtil.replaceWildcards(queryVal, sourceWC, WildcardUtil.WCARD_ANSI_LIKE);
			
			//append wildcard chars to beginning and end if not yet and if not enclosed with double quotes and has more than 3 chars
			if(propDesc.isForceWildcard() && queryVal.length() > 2 
				&& queryVal.charAt(0) != '"' && queryVal.charAt(queryVal.length()-1) != '"') 
			{
				if(queryVal.charAt(0) != wc) {
					queryVal = wc+queryVal;
				}
				if(queryVal.charAt(queryVal.length()-1) != wc) {
					queryVal = queryVal+wc;
				}
			}
		}
		
		ElementDescriptor left = new PropertyName(propDesc.getSystemName());
		ElementDescriptor right = Literal.newInstance(new TextProperty(queryVal));
		
		if(propDesc.isCaseInsensitive() && filterCaps.supportsFunction(SCALAR_FUNCT_STRING_UPPERCASE)
			&& propDesc.getType().getValueType().equals(PropertyType.VALUE_TYPE_TEXT)) 
		{
			if (!propDesc.getInfoBoolean(KEY_UPPERCASED, false)) {
				left = new Function.StringUpperCase(left);
			}
			right = new Function.StringUpperCase(right);
		}
		
		return new ComparisonOperation(left, operation, right);
	}

	@Override
	protected FilterCapabilities getMinimalFilterCaps() {
		return MINIMAL_FILTER_CAPS;
	}
	
	private static Literal<String> createContainsQueryLiteral(String queryValue) {
		return Literal.newInstance(new TextProperty(prepareContainsOpQueryValue(queryValue)));
	}
	
	public static String prepareContainsOpQueryValue(String queryValue) {
		if (queryValue == null)
			return null;
		queryValue = queryValue.replaceAll(",", " ").replaceAll(";", " ");
		
		StringBuffer sb = new StringBuffer();
		for(String part : queryValue.split(" ")) {
			if(part.trim().length() > 0) {
				if(sb.length() > 0) {
					sb.append(" AND ");
				}
				sb.append(part.trim()).append("%");
			}
		}
		return sb.toString();
	}

}
