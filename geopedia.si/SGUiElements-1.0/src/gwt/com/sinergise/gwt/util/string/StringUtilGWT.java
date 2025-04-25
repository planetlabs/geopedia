package com.sinergise.gwt.util.string;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.HasText;
import com.sinergise.common.util.string.StringUtil;

public class StringUtilGWT {
	static final long[] upperBounds = {
		512L           /*Bytes*/, 
		524288L        /*kB*/, 
		536870912L     /*MB*/,
        549755813888L  /*GB*/, 
        Long.MAX_VALUE /*TB*/};
	
	static final long[] conversionFactors = {
		1L, 
		1024L, 
		1048576L, 
		1073741824L,
	    1099511627776L};
	
	static final String[] suffix    = {
		" Bytes", 
		" kB", 
		" MB", 
		" GB", 
		" TB"};
		
	static final NumberFormat formatter = NumberFormat.getFormat("0.00");
		
	private StringUtilGWT() {}

	/**
	 * Wrapper for @a trimNullEmpty 
	 * @param box to take the text from
	 */
	public static String trimNullEmpty(HasText box) {
		if (box == null) {
			return null;
		}
		return StringUtil.trimNullEmpty(box.getText());
	}
	
	public static String addAsterisks(String str) {
		if (str == null)
			return null;
		return "*" + str + "*";
	}
	
	public static JsArrayString toJsArray(String[] strArr) {
		if (strArr == null) {
			return null;
		}
		final int len = strArr.length;
		JsArrayString arr = JavaScriptObject.createArray().cast();
		arr.setLength(len);
		for (int i = 0; i < len; i++) {
			arr.set(i, strArr[i]);
		}
		return arr;
	}
	
	public static String[] fromJsArray(JsArrayString jsArr) {
		if (jsArr == null) {
			return null;
		}
		final int len = jsArr.length();
		String[] ret = new String[len];
		for (int i = 0; i < len; i++) {
			ret[i] = jsArr.get(i);
		}
		return ret;
	}
	
	public static String humanReadableByteSize(long bytes) {
		int index = 0;
	    while (upperBounds[index] < bytes) {
	        ++index;
	    }
	    
	    if (index == 0) {
	    	return bytes + suffix[index];
	    }
	    return formatter.format((double) bytes / conversionFactors[index]) + suffix[index];	
	}
}
