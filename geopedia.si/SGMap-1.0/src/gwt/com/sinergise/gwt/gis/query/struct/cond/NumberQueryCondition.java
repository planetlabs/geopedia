package com.sinergise.gwt.gis.query.struct.cond;

import static com.sinergise.common.gis.filter.FilterCapabilities.SCALAR_OP_COMP_EQUALTO;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.sinergise.common.gis.filter.ComparisonOperation;
import com.sinergise.common.gis.filter.ElementDescriptor;
import com.sinergise.common.gis.filter.ExpressionDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;
import com.sinergise.common.gis.filter.Literal;
import com.sinergise.common.gis.filter.LogicalOperation;
import com.sinergise.common.gis.filter.PropertyName;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.common.util.property.descriptor.PropertyType;
import com.sinergise.common.util.string.StringUtil;

public class NumberQueryCondition extends AbstractQueryCondition {
	
	private static final String TRIMMED_NUMBER = "\\s*(\\b.*\\b)\\s*";
	private static final RegExp BINARY_REGEXP = RegExp.compile("^\\s*([<>]=?|!=)" + TRIMMED_NUMBER + "$");
	private static final RegExp TERNARY_REGEXP = RegExp.compile("^" + TRIMMED_NUMBER + "(\\.\\.)" + TRIMMED_NUMBER + "$");
	public static final FilterCapabilities MINIMAL_FILTER_CAPS = new FilterCapabilities(SCALAR_OP_COMP_EQUALTO);
	
	public NumberQueryCondition(PropertyDescriptor<?> propertyDesc, FilterCapabilities filterCaps) {
		super(propertyDesc, filterCaps);
	}

	@Override
	public FilterDescriptor getQueryCondition() throws InvalidFilterDescriptorException {
		String trimmed = StringUtil.trimNullEmpty(value);
		if (trimmed == null) {
			return null;
		}
		//TODO: Allow && and || to control the aggregation operator
		//TODO: replace comma with something else to allow comma as the decimal separator in certain locales
		//Split values by comma and aggregate with OR
		String[] compValues = trimmed.split(",");
		List<ComparisonOperation> ops = new ArrayList<ComparisonOperation>();
		for (String compValue : compValues) {
			ops.add(getOperation(compValue.trim()));
		}
		
		if (ops.isEmpty()) return null;
		else if (ops.size() == 1) return ops.get(0);
		
		return new LogicalOperation(
			ops.toArray(new ExpressionDescriptor[ops.size()]), FilterCapabilities.SCALAR_OP_LOGICAL_OR);
	}
	
	protected ComparisonOperation getOperation(String compValue) throws InvalidFilterDescriptorException {
		return parseOperation(new PropertyName(propDesc.getSystemName()), compValue, propDesc.isValueType(PropertyType.VALUE_TYPE_REAL));
	}
	
	static ComparisonOperation parseOperation(ElementDescriptor left, String opString, boolean isReal) throws InvalidFilterDescriptorException {
		MatchResult curMatch;
		
		if ((curMatch = TERNARY_REGEXP.exec(opString)) != null) {
			return parseTernaryOperation(left, curMatch, isReal);
		} 
		
		if ((curMatch = BINARY_REGEXP.exec(opString)) != null) {
			return parseBinaryOperation(left, curMatch, isReal);
		}
		
		return ComparisonOperation.createEqual(left, parseNumberLiteral(opString, isReal));
	}

	private static ComparisonOperation parseTernaryOperation(ElementDescriptor left, MatchResult curMatch, boolean isReal) {
		String op = curMatch.getGroup(2);
		if ("..".equals(op)) {
			return ComparisonOperation.createBetween(left, parseNumberLiteral(curMatch.getGroup(1), isReal), parseNumberLiteral(curMatch.getGroup(3), isReal));
		}
		throw new UnsupportedOperationException("Unknown ternary op: "+op);
	}

	private static ComparisonOperation parseBinaryOperation(ElementDescriptor left, MatchResult binaryMatch, boolean isReal) {
		int operation = parseBinaryOperator(binaryMatch.getGroup(1));
		ElementDescriptor right = parseNumberLiteral(binaryMatch.getGroup(2), isReal);
		return new ComparisonOperation(left, operation, right);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Number> Literal<T> parseNumberLiteral(String numString, boolean isReal) throws InvalidFilterDescriptorException {
		try {
			return isReal 
				? (Literal<T>)Literal.newInstance(Double.valueOf(numString))
				: (Literal<T>)Literal.newInstance(Long.valueOf(numString));
		} catch (NumberFormatException nfe) {
			throw (InvalidFilterDescriptorException)new InvalidFilterDescriptorException("Could not parse number "+numString).initCause(nfe);
		}
	} 

	private static int parseBinaryOperator(String opString) {
		if ("<".equals(opString)) {
			return FilterCapabilities.SCALAR_OP_COMP_LESSTHAN;
		}
		if ("<=".equals(opString)) {
			return FilterCapabilities.SCALAR_OP_COMP_LESSTHAN_EQUALTO;
		}
		if (">".equals(opString)) {
			return FilterCapabilities.SCALAR_OP_COMP_GREATERTHAN;	
		}
		if (">=".equals(opString)) {
			return FilterCapabilities.SCALAR_OP_COMP_GREATERTHAN_EQUALTO;
		}
		if ("!=".equals(opString)) {
			return FilterCapabilities.SCALAR_OP_COMP_NOTEQUALTO;
		}
		return FilterCapabilities.SCALAR_OP_COMP_EQUALTO;
	}

	@Override
	protected FilterCapabilities getMinimalFilterCaps() {
		return MINIMAL_FILTER_CAPS;
	}

}
