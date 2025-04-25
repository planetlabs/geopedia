package com.sinergise.common.util.format;

import java.util.Date;

import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.lang.TimeSpec;

public class DateFormatUtil {
	public static final String RFC_1123_DATETIME_PATTERN = "EEE, dd MMM yyyy HH:mm:ss";
	public static final String RFC_1123_DATETIME_PATTERN_GMT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";

	public static final String ISO_DATE_PATTERN = "yyyy-MM-dd";
	public static final String ISO_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

	public static final String SLO_DATE_PATTERN = "dd.MM.yyyy";
	public static final String SLO_DATETIME_PATTERN = "dd.MM.yyyy HH:mm:ss";

	public static final SGDateTimeConstants getConstants() {
		return getProvider().getDefaultDateTimeConstants();
	}

	private static DateFormatProvider getProvider() {
		return I18nProvider.App.getDateFormatProvider();
	}

	public static final DateFormatter create(final String pattern) {
		return create(pattern, getProvider().getDefaultDateTimeConstants());
	}

	public static final DateFormatter create(final String pattern, SGDateTimeConstants constants) {
		return getProvider().createDateFormatter(pattern, constants);
	}

	public static String getDefaultDateTimePattern() {
		return getDefaultDatePattern() + " " + getDefaultTimePattern();
	}

	public static String getDefaultShortDateTimePattern() {
		return getDefaultShortDatePattern() + " " + getDefaultShortTimePattern();
	}

	public static String getDefaultDatePattern() {
		return getProvider().getDefaultDateTimeConstants().dateFormats()[0];
	}

	public static String getDefaultShortDatePattern() {
		return ArrayUtil.lastElement(getProvider().getDefaultDateTimeConstants().dateFormats());
	}

	public static String getDefaultTimePattern() {
		return getProvider().getDefaultDateTimeConstants().timeFormats()[0];
	}

	public static String getDefaultShortTimePattern() {
		return ArrayUtil.lastElement(getProvider().getDefaultDateTimeConstants().timeFormats());
	}

	public static String formatDateISO(final Date date) {
		return date != null ? DateFormatter.FORMATTER_ISO_DATE.formatDate(date) : "";
	}

	public static String formatDateTimeISO(final Date date) {
		return date != null ? DateFormatter.FORMATTER_ISO_DATETIME.formatDate(date) : "";
	}

	public static Date parse(final String val) {
		Exception first = null;
		try {
			return DateFormatter.FORMATTER_ISO_DATE.parse(val);
		} catch(final Exception e) {
			first = e;
		}
		try {
			return DateFormatter.FORMATTER_ISO_DATETIME.parse(val);
		} catch(final Exception e) {
			first = e;
		}
		try {
			return DateFormatter.FORMATTER_DEFAULT_DATE.parse(val);
		} catch(final Exception e) {
			first = e;
		}
		try {
			return DateFormatter.FORMATTER_DEFAULT_DATETIME.parse(val);
		} catch(final Exception e) {
			first = e;
		}
		try {
			return DateFormatter.FORMATTER_DEFAULT_TIME.parse(val);
		} catch(final Exception e) {
			first = e;
		}
		throw new IllegalArgumentException(first);
	}

	public static String format(String pattern, Date value) {
		if (value == null)
			return "";
		return create(pattern).formatDate(value);
	}
	
	public static String format(String pattern, TimeSpec value) {
		if (value == null)
			return "";
		return create(pattern).formatTimeSpec(value);
	}
}
