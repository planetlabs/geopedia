package com.sinergise.generics.java;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sinergise.common.util.format.DateTimeFormatPatterns;
import com.sinergise.common.util.format.Locale;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.java.util.format.JavaDateTimeFormatPatterns;

public class MetaAttributeUtils {

	
	public static String getDateTimeFormat(String formatString, Locale locale, int collectionID) {
		if (formatString==null || formatString.length()==0)
			return formatString;
		
		
		
	

		Pattern pattern = Pattern.compile(MetaAttributes.REGEX_DATETIME_FORMAT);
		Matcher matcher = pattern.matcher(formatString);
		if (matcher.matches()) {
			String key = matcher.group(1);
			String val = matcher.group(2); 
			int size;
			int type;
			if (val.equals("short"))
				size=DateTimeFormatPatterns.SIZE_SHORT;
			else if (val.equals("medium")) 
				size=DateTimeFormatPatterns.SIZE_MEDIUM;
			else if (val.equals("long")) 
				size=DateTimeFormatPatterns.SIZE_LONG;
			else 
				throw new RuntimeException("Illegal size in ValueFormat: "+val);
			
			if (key.equals(MetaAttributes.META_DATE_FORMAT))
				type = DateTimeFormatPatterns.DATE;
			else if (key.equals(MetaAttributes.META_TIME_FORMAT))
				type = DateTimeFormatPatterns.TIME;
			else if (key.equals(MetaAttributes.META_DATETIME_FORMAT))
				type = DateTimeFormatPatterns.DATE_TIME;
			else 
				throw new RuntimeException("Illegal type in ValueFormat: "+val);
			
			
			return JavaDateTimeFormatPatterns.getFormatPatternString(collectionID, locale, type, size);
			
 		}
		
		return formatString;
	}
}
