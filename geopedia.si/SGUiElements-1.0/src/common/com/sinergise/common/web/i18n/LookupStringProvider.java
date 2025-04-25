package com.sinergise.common.web.i18n;

import java.util.Map;
import java.util.MissingResourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.i18n.client.Dictionary;

public interface LookupStringProvider {

	String getString(String key);
	
	String getString(String key, String defString);
	
	public static abstract class ALookupStringProvider implements LookupStringProvider {
		
		public String getString(String key, String defString) {
			try {
				return getString(key);
			} catch (MissingResourceException e) {
				return defString;
			}
		}
		
	}
	
	public static class FromConstants extends ALookupStringProvider {
		
		private final ConstantsWithLookup constants;
		
		public FromConstants(ConstantsWithLookup constants) {
			this.constants = constants;
		}
		
		public String getString(String key) {
			return constants.getString(key);
		}
		
	}
	
	public static class FromDictionary extends ALookupStringProvider {
		
		private static final Logger logger = LoggerFactory.getLogger(FromDictionary.class);
		
		final static String QUOTE_REPLACE = "@@quote@@";
		
		private final Dictionary dictionary;
		
		public FromDictionary(Dictionary dictionary) {
			this.dictionary = dictionary;
		}
		
		public String getString(String key) {
			String val = dictionary.get(key);
			val = val.replaceAll(QUOTE_REPLACE, "'");
			return val;
		}
		
		public static void createDictionary(String name, Map<String, String> data) {
			
			//construct JSON array
			StringBuffer sb = new StringBuffer();
			
			sb.append("$wnd.").append(name).append("= {");
			int cnt=0;
			for (String key : data.keySet()) {
				if (cnt++ > 0) sb.append(",\n");
				String val = data.get(key);
				val = val.replaceAll("\n", "");
				val = val.replaceAll("\r", "");
				val = val.replaceAll("\t", "");
				val = val.replaceAll("'", QUOTE_REPLACE);
				
				sb.append("'").append(key).append("'").append(": '").append(val).append("'");
			}
			sb.append("};");
			
			try {
				createDictionary(sb.toString());
			} catch (Throwable t) {
				logger.error("Error initializing dictonary. Check dictionary values.", t);
			}
		}
		
		private static native void createDictionary(String jsonDict) /*-{
			eval(jsonDict);
		}-*/;
		
	}
	
}
