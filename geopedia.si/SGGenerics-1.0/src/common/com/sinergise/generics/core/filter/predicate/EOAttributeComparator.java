package com.sinergise.generics.core.filter.predicate;

import com.sinergise.generics.core.AbstractEntityObject;
import com.sinergise.generics.core.PrimitiveValue;
import com.sinergise.generics.core.util.EntityUtils;

public class EOAttributeComparator implements IEntityObjectFilter{

	private static final long serialVersionUID = -9043002727581505413L;
	
	public static enum Operator {EQUALS}
	
	private String attributeValue;
	private String attributeName;
	private Operator operator;
	
	@Deprecated
	protected EOAttributeComparator() {		
	}
	
	public EOAttributeComparator (String attributeName, String attributeValue) {
		this(attributeName, attributeValue, Operator.EQUALS);
	}
	
	public EOAttributeComparator (String attributeName, String attributeValue, Operator operator) {
		this.attributeValue=attributeValue;
		this.attributeName=attributeName;
		this.operator = operator;
	}
	
	public Operator getOperator() {
		return operator;
	}
	
	public String getAttributeName() {
		return attributeName;
	}
	
	public String getAttributeValue() {
		return attributeValue;
	}
	
	@Override
	public boolean eval(AbstractEntityObject eo) {
		PrimitiveValue pv = EntityUtils.getPrimitiveValue(eo, attributeName);
		if (pv==null) return false;
		if (pv.value==null) {
			if (attributeValue==null)
				if (operator==Operator.EQUALS)
					return true;
			return false;
		}
		if (operator == Operator.EQUALS)
			return pv.value.equals(attributeValue);
		return false;
	}

	
	@Override
	public String toString() {
		return attributeName+"=='"+attributeValue+"'";
	}
}
