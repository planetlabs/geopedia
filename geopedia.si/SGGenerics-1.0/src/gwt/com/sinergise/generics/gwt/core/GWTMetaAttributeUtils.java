package com.sinergise.generics.gwt.core;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.sinergise.common.util.format.DateTimeFormatPatterns;
import com.sinergise.common.util.format.Locale;
import com.sinergise.generics.core.MetaAttributes;


public class GWTMetaAttributeUtils {

	public static DateTimeFormat getDateTimeFormat(String formatString) {
		Locale locale = Locale.forName(LocaleInfo.getCurrentLocale().getLocaleName());
		if (formatString==null || formatString.length()==0)  {
			String pattern = DateTimeFormatPatterns.getFormatPatternString(locale, DateTimeFormatPatterns.DATE, DateTimeFormatPatterns.SIZE_MEDIUM);
			if (pattern == null) {
				throw new RuntimeException("Date pattern not found for locale: " + locale);
			}
			return DateTimeFormat.getFormat(pattern);
		}
		
		if (formatString.contains("=")) {
			String arry[] = formatString.split("=");
			String key = arry[0];
			String val = arry[1];
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
			
			String pattern =  DateTimeFormatPatterns.getFormatPatternString(locale, type, size);
			return DateTimeFormat.getFormat(pattern);
		}
		
		return DateTimeFormat.getFormat(formatString);
	}
}
