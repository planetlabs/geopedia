package com.sinergise.generics.gwt.widgetprocessors.helpers;

import java.util.Collection;
import java.util.HashMap;

import com.google.gwt.regexp.shared.RegExp;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.util.FilterProcessorBase;

public class GWTFilterProcessor extends FilterProcessorBase{
	private boolean ignoreCase = false;
	private static final org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(GWTFilterProcessor.class); 

	
	HashMap<String, GenericObjectProperty> propMap=null;
	
	public GWTFilterProcessor(Collection<GenericObjectProperty> propList) {
		if (propList!=null) {
		propMap = new HashMap<String, GenericObjectProperty>();
			for (GenericObjectProperty gop:propList) {
				propMap.put(gop.getName(), gop);
			}
		}
	}
	
	static char[] toReplace = new char[] { '.', '+', '[',']','^','$','(',')','{','}','=','!','<','>','|',':'};

	
	private static String conditionForRegexp(String string) {
		for (char c:toReplace) {
			string = string.replace(""+c, "\\"+c);
		}
		string = string.replace("?", ".");
		string = string.replace("*", "(.*)");
		logger.trace("Condition: {}",string);
		return string;
	}
	
	@Override
	protected boolean matchesString(String filterValue, String valueToMatch) {
		if(ignoreCase) {
			filterValue = filterValue.toUpperCase();
			valueToMatch = valueToMatch.toUpperCase();
		}
		RegExp regex = RegExp.compile(conditionForRegexp(filterValue));
		return regex.test(valueToMatch);
	}

	@Override
	protected String getMetaAttribute(String entityAttributeName,
			String metaAttributeName) {
		if (propMap==null) {
			return null;
		}
		GenericObjectProperty gop   = propMap.get(entityAttributeName);
		if (gop==null)
			return null;
		return gop.getAttributes().get(metaAttributeName);
	}
	
	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

}
