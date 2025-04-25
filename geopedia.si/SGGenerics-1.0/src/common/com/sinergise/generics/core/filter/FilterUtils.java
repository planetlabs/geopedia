package com.sinergise.generics.core.filter;

import static com.sinergise.common.util.string.WildcardUtil.WCARD_WINDOWS;

import com.sinergise.common.util.string.StringUtil;
import com.sinergise.common.util.string.WildcardUtil.Wildcards;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.util.EntityUtils;

public class FilterUtils {
	
	public static boolean valueContainsWildcards(TypeAttribute attribute, String string) {
		return valueContainsWildcards(string, getWildcardsFor(attribute, string));
	}
	public static boolean valueContainsWildcards(String string, Wildcards wCards) {
		if (string==null)
			return false;
		return wCards.stringContainsWildcards(string);
	}
	
	public static boolean valueContainsEscape(String string, Wildcards wCards) {
		if (string==null)
			return false;
		return wCards.stringContainsEscape(string);
	}
	
	public static Wildcards getWildcardsFor(TypeAttribute attribute, String filterVal) {
		return WCARD_WINDOWS;
	}
	public static boolean valueContainsWildcards(String string) {
		return valueContainsWildcards(string, WCARD_WINDOWS);
	}
	
	
	
	public static SimpleFilter buildSimpleFilter(StringBuffer expression,  EntityObject newFilterEO, EntityObject sourceEO, Integer idx) {
		EntityType newFilterET = newFilterEO.getType(); 
		byte[] operators = new byte[newFilterET.getAttributeCount()];
		for (int i=0;i<operators.length;i++) 
			operators[i]= DataFilter.OPERATOR_AND;
		
		while (idx<expression.length()) {
			
			int delimIdx = StringUtil.indexOf(expression, new char[]{'\u2227','\u2228'}, idx);
			String subExp = null;
			Character operator = null;
			if (delimIdx==-1) {
				subExp = expression.substring(idx);
				idx=expression.length();
			} else {
				subExp = expression.substring(idx, delimIdx).trim();
				operator = expression.charAt(delimIdx);
				idx=delimIdx+1;				
			}
			
			if (!subExp.contains("=")) 
				throw new IllegalArgumentException("'"+subExp+"' (from: "+expression.toString()+") does not contain '='");
			String []pair=subExp.split("=");
			String key=pair[0].trim();
			String value=pair[1].trim();
			TypeAttribute ta = newFilterET.getAttribute(key);
			if (ta==null) 
				throw new IllegalArgumentException("Entity type: '"+newFilterET+"' does not contain attribute: '"+key+"'");
			if (operator!=null) {
				if ('\u2227'==operator) {
					operators[ta.getId()]=DataFilter.OPERATOR_AND;
				} else {
					operators[ta.getId()]=DataFilter.OPERATOR_OR;
				}
			}
			
			if (!value.startsWith("@")) {
				EntityUtils.setStringValue(newFilterEO, key, value);
			} else {
				String attributeName = value.substring(1);
				String attValue = EntityUtils.getStringValue(sourceEO, attributeName);
				EntityUtils.setStringValue(newFilterEO, key, attValue);
			}
//			System.out.println(subExp+" operator:"+operator);
		}
		
		SimpleFilter newSimpleFilter = new SimpleFilter(newFilterEO,operators);
		return newSimpleFilter;
	}
	
	public static DataFilter buildFilterFromExpression(String expression, EntityObject newFilterEO, EntityObject sourceEO) {
		return buildSimpleFilter(new StringBuffer(expression), newFilterEO, sourceEO, 0);
	}
}
