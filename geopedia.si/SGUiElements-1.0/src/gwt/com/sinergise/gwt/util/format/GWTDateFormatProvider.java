/**
 * 
 */
package com.sinergise.gwt.util.format;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.i18n.client.constants.DateTimeConstants;
import com.sinergise.common.util.format.DateFormatProvider;
import com.sinergise.common.util.format.DateFormatter;
import com.sinergise.common.util.format.SGDateTimeConstants;
import com.sinergise.common.util.lang.TimeSpec;

@SuppressWarnings("deprecation")
public class GWTDateFormatProvider implements DateFormatProvider {
	public static final DateTimeConstants GWT_DEFAULT_CONSTANTS = LocaleInfo.getCurrentLocale().getDateTimeConstants();
	
	public static final class ConstsGWTFromSG implements DateTimeConstants {
		SGDateTimeConstants ref;
		
		public ConstsGWTFromSG(final SGDateTimeConstants ref) {
			super();
			this.ref = ref;
		}
		
		@Override
		public String[] ampms() {
			return ref.ampms();
		}
		
		@Override
		public String[] dateFormats() {
			return ref.dateFormats();
		}
		
		@Override
		public String[] eras() {
			return ref.eras();
		}
		
		@Override
		public String firstDayOfTheWeek() {
			return ref.firstDayOfTheWeek();
		}
		
		@Override
		public String[] months() {
			return ref.months();
		}
		
		@Override
		public String[] shortMonths() {
			return ref.shortMonths();
		}
		
		@Override
		public String[] shortWeekdays() {
			return ref.shortWeekdays();
		}
		
		@Override
		public String[] timeFormats() {
			return ref.timeFormats();
		}
		
		@Override
		public String[] weekdays() {
			return ref.weekdays();
		}
		
		@Override
		public String[] eraNames() {
			return ref.eraNames();
		}
		
		@Override
		public String[] narrowMonths() {
			return ref.narrowMonths();
		}
		
		@Override
		public String[] narrowWeekdays() {
			return ref.narrowWeekdays();
		}
		
		@Override
		public String[] quarters() {
			return ref.quarters();
		}
		
		@Override
		public String[] shortQuarters() {
			return ref.shortQuarters();
		}
		
		@Override
		public String[] standaloneMonths() {
			return ref.standaloneMonths();
		}
		
		@Override
		public String[] standaloneNarrowMonths() {
			return ref.standaloneNarrowMonths();
		}
		
		@Override
		public String[] standaloneNarrowWeekdays() {
			return ref.standaloneNarrowWeekdays();
		}
		
		@Override
		public String[] standaloneShortMonths() {
			return ref.standaloneShortMonths();
		}
		
		@Override
		public String[] standaloneShortWeekdays() {
			return ref.standaloneShortWeekdays();
		}
		
		@Override
		public String[] standaloneWeekdays() {
			return ref.standaloneWeekdays();
		}
		
		@Override
		public String[] weekendRange() {
			return ref.weekendRange();
		}
	}
	
	public static final class ConstsSGFromGWT implements SGDateTimeConstants {
		DateTimeConstants ref;
		
		public ConstsSGFromGWT(final DateTimeConstants ref) {
			super();
			this.ref = ref;
		}
		
		@Override
		public String[] ampms() {
			return ref.ampms();
		}
		
		@Override
		public String[] dateFormats() {
			return ref.dateFormats();
		}
		
		@Override
		public String[] eras() {
			return ref.eras();
		}
		
		@Override
		public String firstDayOfTheWeek() {
			return ref.firstDayOfTheWeek();
		}
		
		@Override
		public String[] months() {
			return ref.months();
		}
		
		@Override
		public String[] shortMonths() {
			return ref.shortMonths();
		}
		
		@Override
		public String[] shortWeekdays() {
			return ref.shortWeekdays();
		}
		
		@Override
		public String[] timeFormats() {
			return ref.timeFormats();
		}
		
		@Override
		public String[] weekdays() {
			return ref.weekdays();
		}
		
		@Override
		public String[] eraNames() {
			return ref.eraNames();
		}
		
		@Override
		public String[] narrowMonths() {
			return ref.narrowMonths();
		}
		
		@Override
		public String[] narrowWeekdays() {
			return ref.narrowWeekdays();
		}
		
		@Override
		public String[] quarters() {
			return ref.quarters();
		}
		
		@Override
		public String[] shortQuarters() {
			return ref.shortQuarters();
		}
		
		@Override
		public String[] standaloneMonths() {
			return ref.standaloneMonths();
		}
		
		@Override
		public String[] standaloneNarrowMonths() {
			return ref.standaloneNarrowMonths();
		}
		
		@Override
		public String[] standaloneNarrowWeekdays() {
			return ref.standaloneNarrowWeekdays();
		}
		
		@Override
		public String[] standaloneShortMonths() {
			return ref.standaloneShortMonths();
		}
		
		@Override
		public String[] standaloneShortWeekdays() {
			return ref.standaloneShortWeekdays();
		}
		
		@Override
		public String[] standaloneWeekdays() {
			return ref.standaloneWeekdays();
		}
		
		@Override
		public String[] weekendRange() {
			return ref.weekendRange();
		}
	}
	
	protected static final DateTimeConstants toGWT(final SGDateTimeConstants sConsts) {
		return new ConstsGWTFromSG(sConsts);
	}
	
	protected static final SGDateTimeConstants fromGWT(final DateTimeConstants gConsts) {
		return new ConstsSGFromGWT(gConsts);
	}
	
	public static final class GWTDateFormatter extends DateTimeFormat implements DateFormatter {
		static final TimeZone utcTime = TimeZone.createTimeZone(0);
		public GWTDateFormatter(final String pattern, final SGDateTimeConstants consts) {
			super(pattern, consts == null ? GWT_DEFAULT_CONSTANTS : toGWT(consts));
		}
		
		@Override
		public String formatTimeSpec(TimeSpec tSpec) {
			return formatDate(new Date(tSpec.toJavaTimeWithJvmLocalTimeZoneOffset()));
		}
		
		@Override
		public String formatDate(final Date date) {
			return formatDate(date, date.getTimezoneOffset());
		}
		
		/**
		 * @param the provided date; note that the date object will still  
		 * @param timeZoneOffset UTC offset of the target timezone, in minutes
		 */
		@Override
		public String formatDate(Date date, int timeZoneOffset) {
			return super.format(date, timeZoneOffset == 0 ? utcTime : TimeZone.createTimeZone(timeZoneOffset));
		}
		
		@Override
		public Date parse(String text) {
			//Always change to UTC
			return parse(text, 0);
		}
		
		@Override
		public Date parse(String dateString, int timeZoneOffset) {
			Date dt = super.parse(dateString);
			int off = dt.getTimezoneOffset();
			if (off != timeZoneOffset) {
				dt.setTime(dt.getTime() - 60L*1000*timeZoneOffset);
			}
			return dt;
		}
	}
	
	@Override
	public DateFormatter createDateFormatter(final String pattern, final SGDateTimeConstants consts) {
		return new GWTDateFormatter(pattern, consts);
	}
	
	@Override
	public SGDateTimeConstants getDefaultDateTimeConstants() {
		return fromGWT(GWT_DEFAULT_CONSTANTS);
	}
}