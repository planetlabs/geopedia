package com.sinergise.common.util.format;

public interface DateFormatProvider {
	DateFormatter createDateFormatter(String pattern, SGDateTimeConstants consts);
	
	SGDateTimeConstants getDefaultDateTimeConstants();
}
