package com.sinergise.common.util.format;

import java.util.Date;

import com.sinergise.common.util.lang.TimeSpec;

/**
 * Implementations are thread safe.
 * 
 * @author Miha
 */
//TODO Time Zones problem
public interface DateFormatter {
	public static DateFormatter FORMATTER_ISO_DATE         = DateFormatUtil.create(DateFormatUtil.ISO_DATE_PATTERN);
	public static DateFormatter FORMATTER_ISO_DATETIME     = DateFormatUtil.create(DateFormatUtil.ISO_DATETIME_PATTERN);
	public static DateFormatter FORMATTER_DEFAULT_DATE     = DateFormatUtil.create(DateFormatUtil.getDefaultShortDatePattern());
	public static DateFormatter FORMATTER_DEFAULT_DATETIME = DateFormatUtil.create(DateFormatUtil.getDefaultShortDateTimePattern());
	public static DateFormatter FORMATTER_DEFAULT_TIME     = DateFormatUtil.create(DateFormatUtil.getDefaultShortTimePattern());

	/**
	 * @param date
	 * @return the date formatted using the date object's internal timezone setting
	 */
	public String formatTimeSpec(TimeSpec tSpec);

	/**
	 * @param date
	 * @return the date formatted using the date object's internal timezone setting
	 */
	public String formatDate(Date date);

	/**
	 * @param date
	 * @return the date using the provided timezone offset
	 */
	public String formatDate(Date date, int timeZoneOffset);

	/**
	 * 
	 * @param dateString
	 * @return the string parsed using UTC timezone
	 * @throws Exception
	 */
	public Date parse(String dateString) throws Exception;
	
	/**
	 * 
	 * @param dateString
	 * @return the string parsed using the provided timezone
	 * @throws Exception
	 */
	public Date parse(String dateString, int timeZoneOffset) throws Exception;
}
