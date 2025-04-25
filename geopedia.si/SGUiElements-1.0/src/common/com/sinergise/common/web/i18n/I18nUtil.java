package com.sinergise.common.web.i18n;

import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.util.state.gwt.StateGWT;

public class I18nUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(I18nUtil.class);
	
	static final String EXT_STRING_PREFIX = "@ext:";
	static final String EXT_STRING_SUFFIX = ";";

	private I18nUtil() {
		//hide constructor
	}
	
	public static void resolveExternalizedStrings(StateGWT state, LookupStringProvider lookup) {
		//resolve for properties
		for (String key : state.getPropertyMap().keySet()) {
			state.putString(key, resolveExternalizedString(state.getString(key, null), lookup));
		}
		//resolve for children
		for (Iterator<String> childIter = state.childKeyIterator(); childIter.hasNext();) {
			resolveExternalizedStrings(state.getState(childIter.next()), lookup);
		}
	}
	
	public static String resolveExternalizedString(String string, LookupStringProvider lookup) {
		if (isNullOrEmpty(string) || !string.contains(EXT_STRING_PREFIX)) {
			return string;
		}
		
		int prefPos = string.indexOf(EXT_STRING_PREFIX);
		int suffPos = string.indexOf(EXT_STRING_SUFFIX);
		if (suffPos < 0) { //allow missing suffix and treat entire string as lookup key
			suffPos = string.length();
			string += EXT_STRING_SUFFIX;
		}
		
		String key = string.substring(prefPos+EXT_STRING_PREFIX.length(), suffPos);
		String toReplace = EXT_STRING_PREFIX+key+EXT_STRING_SUFFIX;
		try {
			string = string.replace(toReplace, lookup.getString(key, key));
			if (string.contains(EXT_STRING_PREFIX)) {
				return resolveExternalizedString(string, lookup);
			}
		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
		
		return string;
	}
	
}
