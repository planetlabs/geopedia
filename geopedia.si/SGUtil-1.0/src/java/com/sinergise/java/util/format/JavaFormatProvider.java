package com.sinergise.java.util.format;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.format.DateFormatProvider;
import com.sinergise.common.util.format.DateFormatter;
import com.sinergise.common.util.format.I18nProvider;
import com.sinergise.common.util.format.NumberFormatProvider;
import com.sinergise.common.util.format.NumberFormatUtil;
import com.sinergise.common.util.format.NumberFormatter;
import com.sinergise.common.util.format.SGDateTimeConstants;
import com.sinergise.common.util.lang.TimeSpec;
import com.sinergise.common.util.lang.number.SGNumber;
import com.sinergise.common.util.lang.number.SGNumberApiMapping.RepresentableAsBigDecimal;
import com.sinergise.common.util.lang.number.SGNumberApiMapping.RepresentableAsDouble;

public class JavaFormatProvider implements I18nProvider, NumberFormatProvider, DateFormatProvider {
	public static final TimeZone TZ_UTC = TimeZone.getTimeZone("UTC");
	static {
		if (!I18nProvider.App.isInitialized()) {
			I18nProvider.App.initProvider(new JavaFormatProvider());
		}
	}
	public static void init() {
	// force static init above
	}
	
	static DecimalFormatSymbols dfs;
	static JavaDateTimeSymbols  dds;
	
	public static class JavaDateFormat implements DateFormatter, Serializable {
		private static final long serialVersionUID = 1L;
		
		final String pattern;
		SGDateTimeConstants syms;
		
		ThreadLocal<SimpleDateFormat> format = new ThreadLocal<SimpleDateFormat>() {
			@Override
			protected SimpleDateFormat initialValue() {
				SimpleDateFormat sdf = new SimpleDateFormat(pattern);
				sdf.setTimeZone(TZ_UTC);
				if (syms != null) {
					final DateFormatSymbols curDfs = new DateFormatSymbols();
					curDfs.setAmPmStrings(syms.ampms());
					curDfs.setEras(syms.eras());
					curDfs.setMonths(syms.months());
					curDfs.setShortMonths(syms.shortMonths());
					curDfs.setShortWeekdays(weekdaysToJava(syms.shortWeekdays()));
					curDfs.setWeekdays(weekdaysToJava(syms.weekdays()));
					sdf.setDateFormatSymbols(curDfs);
					sdf.getCalendar().setFirstDayOfWeek(ArrayUtil.firstIndexOf(curDfs.getWeekdays(), syms.firstDayOfTheWeek()));
				}
				
				return sdf;
			}
		};
		public JavaDateFormat(final String pattern, final SGDateTimeConstants syms) {
			this.pattern = pattern;
			this.syms = syms;
		}
		
		@Override
		public String formatTimeSpec(TimeSpec tSpec) {
			return formatDate(new Date(tSpec.toJavaTimeWithJvmLocalTimeZoneOffset()));
		}
		
		@Override
		@SuppressWarnings("deprecation")
		public String formatDate(final Date date) {
			if (date==null) return null;
			return formatDate(date, -date.getTimezoneOffset());
		}
		
		@Override
		public String formatDate(Date date, int timeZoneOffsetMinutes) {
			if (timeZoneOffsetMinutes == 0) {
				return format.get().format(date);
			}
			SimpleDateFormat f = format.get();
			f.setTimeZone(new SimpleTimeZone(timeZoneOffsetMinutes * 60 * 1000, ""));
			try {
				return f.format(date);
			} finally {
				f.setTimeZone(TZ_UTC);
			}
		}

		@Override
		@SuppressWarnings("deprecation")
		public Date parse(String dateString, int timeZoneOffset) throws Exception {
			Date dt = parse(dateString);
			int offMinutes = dt.getTimezoneOffset();
			if (offMinutes != timeZoneOffset) {
				dt.setTime(dt.getTime() - 60L*1000*timeZoneOffset);
			}
			return dt;
		}

		@Override
		public Date parse(String dateString) throws Exception {
			return format.get().parse(dateString);
		}
	}
	
	public static class JavaDateTimeSymbols implements SGDateTimeConstants {
		private final DateFormatSymbols syms;
		private final Calendar          calendar;
		
		private final String[]          dateFormats;
		private final String[]          timeFormats;
		
		public JavaDateTimeSymbols(final Locale locale, final DateFormatSymbols syms) {
			this.syms = syms;
			this.calendar = Calendar.getInstance(locale);
			final int[] frmts = new int[]{DateFormat.FULL, DateFormat.LONG, DateFormat.MEDIUM, DateFormat.SHORT};
			
			dateFormats = new String[frmts.length];
			timeFormats = new String[frmts.length];
			
			for (int i = 0; i < frmts.length; i++) {
				DateFormat f = DateFormat.getDateInstance(frmts[i], locale);
				if (f instanceof SimpleDateFormat) {
					dateFormats[i] = ((SimpleDateFormat)f).toPattern();
				}
				
				f = DateFormat.getTimeInstance(frmts[i], locale);
				if (f instanceof SimpleDateFormat) {
					timeFormats[i] = ((SimpleDateFormat)f).toPattern();
				}
			}
		}
		
		@Override
		public String[] ampms() {
			return syms.getAmPmStrings();
		}
		
		@Override
		public String[] dateFormats() {
			return dateFormats;
		}
		
		@Override
		public String[] eraNames() {
			return syms.getEras();
		}
		
		@Override
		public String[] eras() {
			return syms.getEras();
		}
		
		@Override
		public String firstDayOfTheWeek() {
			return syms.getWeekdays()[calendar.getFirstDayOfWeek()];
		}
		
		@Override
		public String[] months() {
			return syms.getMonths();
		}
		
		@Override
		public String[] narrowMonths() {
			return shortMonths();
		}
		
		@Override
		public String[] narrowWeekdays() {
			return shortWeekdays();
		}
		
		@Override
		public String[] quarters() {
			return new String[]{"Q1", "Q2", "Q3", "Q4"};
		}
		
		@Override
		public String[] shortMonths() {
			return syms.getShortMonths();
		}
		
		@Override
		public String[] shortQuarters() {
			return quarters();
		}
		
		@Override
		public String[] shortWeekdays() {
			return weekdaysFromJava(syms.getShortWeekdays());
		}
		
		@Override
		public String[] standaloneMonths() {
			return syms.getMonths();
		}
		
		@Override
		public String[] standaloneNarrowMonths() {
			return narrowMonths();
		}
		
		@Override
		public String[] standaloneNarrowWeekdays() {
			return narrowWeekdays();
		}
		
		@Override
		public String[] standaloneShortMonths() {
			return shortMonths();
		}
		
		@Override
		public String[] standaloneShortWeekdays() {
			return shortWeekdays();
		}
		
		@Override
		public String[] standaloneWeekdays() {
			return weekdays();
		}
		
		@Override
		public String[] timeFormats() {
			return timeFormats;
		}
		
		@Override
		public String[] weekdays() {
			return weekdaysFromJava(syms.getWeekdays());
		}
		
		@Override
		public String[] weekendRange() {
			return new String[]{weekdays()[5], weekdays()[6]};
		}
	}
	
	public static class JavaNumberFormat implements NumberFormatter {
		private final DecimalFormat nf;
		
		public JavaNumberFormat(final DecimalFormat nf) {
			this.nf = nf;
		}
		
		public JavaNumberFormat(final NumberFormat nf) {
			if (nf instanceof DecimalFormat) {
				this.nf = (DecimalFormat)nf;
			} else {
				throw new IllegalArgumentException("Cannot construct instance of JavaNumberFormat with format other than DecimalFormat");
			}
		}

		@Override
		public String format(final double number) {
			return nf.format(number);
		}
		
		@Override
		public String format(final SGNumber number) {
			if (number instanceof RepresentableAsDouble) {
				return format(((RepresentableAsDouble)number).doubleValue());
			}
			if (number instanceof RepresentableAsBigDecimal) {
				return nf.format(((RepresentableAsBigDecimal)number).bigDecimalValue());
			}
			throw new UnsupportedOperationException("Unsupported SGNumber type: "+number.getClass() +" value: "+number);
		}
	}
	
	@Override
	public DateFormatter createDateFormatter(final String pattern, final SGDateTimeConstants consts) {
		return new JavaDateFormat(pattern, consts);
	}
	
	@Override
	public SGDateTimeConstants getDefaultDateTimeConstants() {
		if (dds == null) {
			dds = new JavaDateTimeSymbols(Locale.getDefault(), new DateFormatSymbols(Locale.getDefault()));
		}
		return dds;
	}
	
	@Override
	public NumberFormatter create(final String pattern, final NumberFormatConstants consts) {
		final DecimalFormatSymbols localDfs = (DecimalFormatSymbols)getDfs().clone();
		localDfs.setDecimalSeparator(consts.decimalSeparator.charAt(0));
		localDfs.setGroupingSeparator(consts.groupingSeparator.charAt(0));
		return new JavaNumberFormat(new DecimalFormat(pattern, localDfs));
	}
	
	private static DecimalFormatSymbols getDfs() {
		if (dfs == null) {
			dfs = ((DecimalFormat)NumberFormat.getInstance()).getDecimalFormatSymbols();
		}
		return dfs;
	}

	@Override
	public NumberFormatConstants getDefaultConstants() {
		return toSgConstants(DecimalFormatSymbols.getInstance());
	}
	
	@Override
	public NumberFormatConstants getConstants(com.sinergise.common.util.format.Locale locale) {
		return toSgConstants(DecimalFormatSymbols.getInstance(new Locale(locale.getLanguage(), locale.getCountry())));
	}
	
	private static NumberFormatConstants toSgConstants(DecimalFormatSymbols symbols) {
		return new NumberFormatConstants(
			String.valueOf(symbols.getDecimalSeparator()),
			String.valueOf(symbols.getGroupingSeparator()));
	}

	@Override
	public NumberFormatter createDefaultDecimal(NumberFormatConstants constants) {
		if (",".equals(constants.decimalSeparator)) {
			return new JavaNumberFormat(NumberFormat.getNumberInstance(Locale.FRENCH));
			
		} else if (",".equals(constants.groupingSeparator)) {
			return new JavaNumberFormat(NumberFormat.getNumberInstance(Locale.US));
		}
		return new JavaNumberFormat(NumberFormat.getNumberInstance());
	}
	
	@Override
	public NumberFormatter createDefaultCurrency() {
		return new JavaNumberFormat(NumberFormat.getCurrencyInstance());
	}
	
	public static final NumberFormatter create(final String pattern) {
		return NumberFormatUtil.create(pattern);
	}
	
	public static final NumberFormatter createPattern(final String pattern, final NumberFormatConstants constants) {
		return NumberFormatUtil.create(pattern, constants);
	}
	
	public static final String defaultDecimalSeparator() {
		return NumberFormatUtil.getDefaultConstants().decimalSeparator;
	}
	
	public static final String defaultGroupingSeparator() {
		return NumberFormatUtil.getDefaultConstants().groupingSeparator;
	}

	public static NumberFormatConstants getConstants(Locale locale) {
		DecimalFormatSymbols consts = DecimalFormatSymbols.getInstance(locale);
		return new NumberFormatConstants(consts.getDecimalSeparator()+"", consts.getGroupingSeparator() +"");
	}
	
	@Override
	public DateFormatProvider getDateFormatProvider() {
		return this;
	}
	
	@Override
	public NumberFormatProvider getNumberFormatProvider() {
		return this;
	}
	
	@Override
	public com.sinergise.common.util.format.Locale getDefaultLocale() {
		Locale l = Locale.getDefault();
		return com.sinergise.common.util.format.Locale.forName(l.getLanguage()+"_"+l.getCountry());
	}
	
	public static String[] weekdaysToJava(String[] gwtWeekdays) {
		return ArrayUtil.concat(new String[] {""}, gwtWeekdays, new String[gwtWeekdays.length+1]);
	}
	
	public static String[] weekdaysFromJava(String[] javaWeekdays) {
		return ArrayUtil.drop(javaWeekdays, 1);
	}
}
