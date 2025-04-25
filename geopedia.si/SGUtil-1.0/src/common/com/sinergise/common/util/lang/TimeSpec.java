package com.sinergise.common.util.lang;

import static com.sinergise.common.util.lang.TimeSpec.Resolution.DAY;
import static com.sinergise.common.util.lang.TimeSpec.Resolution.HOUR;
import static com.sinergise.common.util.lang.TimeSpec.Resolution.MICROSECOND;
import static com.sinergise.common.util.lang.TimeSpec.Resolution.MILLISECOND;
import static com.sinergise.common.util.lang.TimeSpec.Resolution.MINUTE;
import static com.sinergise.common.util.lang.TimeSpec.Resolution.MONTH;
import static com.sinergise.common.util.lang.TimeSpec.Resolution.NANOSECOND;
import static com.sinergise.common.util.lang.TimeSpec.Resolution.SECOND;
import static com.sinergise.common.util.lang.TimeSpec.Resolution.YEAR;
import static com.sinergise.common.util.math.MathUtil.divFloor;
import static com.sinergise.common.util.math.MathUtil.mod;
import static com.sinergise.common.util.string.StringUtil.appendPadded;
import static java.lang.Integer.parseInt;
import static java.lang.Short.parseShort;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import com.sinergise.common.util.CheckUtil;
import com.sinergise.common.util.format.NumberFormatUtil;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.common.util.settings.Settings.SerializeAsString;
import com.sinergise.common.util.string.HasCanonicalStringRepresentation;

@SerializeAsString(TimeSpecSerializer.class)
public class TimeSpec implements Serializable, Comparable<TimeSpec>, HasCanonicalStringRepresentation {
	private static int MAX_YEAR = 240000;
	private static int MIN_YEAR = -240000;
	
	private static final long serialVersionUID = 1L;
	
	private static final int MILLIS_IN_HOUR = 60*60*1000;
	private static final int JAVA_EPOCH_HOUR = 24 * toJulianDay(1970, 1, 1);
	
	public static enum Resolution {
		YEAR("yyyy", 24, 0, 365.25*24*3600*1e9),
		MONTH("yyyy-MM", 24, 0, 30.4375*24*3600*1e9),
		WEEK("yyyy-MM-dd", 24, 0, 7*24*3600*1e9),
		DAY("yyyy-MM-dd", 24, 0, 24*3600*1e9),
		HOUR("yyyy-MM-dd'T'HH", 1, 0, 3600*1e9),
		MINUTE("yyyy-MM-dd'T'HH:mm", 1, 60, 60*1e9),
		SECOND("yyyy-MM-dd'T'HH:mm:ss", 1, 1, 1e9),
		MILLISECOND(0, "yyyy-MM-dd'T'HH:mm:ss.SSS", 1000000, 1e6),
		MICROSECOND(3, "yyyy-MM-dd'T'HH:mm:ss.SSS", 1000, 1e3),
		NANOSECOND(6, "yyyy-MM-dd'T'HH:mm:ss.SSS", 1, 1);
		
		public final String format;
		final int numSubMilli;
		final int trimHours;
		final short trimSec;
		final int trimNano;
		final double nanoFactor;

		private Resolution(String format, int hrs, int sec, double nanoF) {
			this.format = format;
			this.numSubMilli = 0;
			this.trimHours = hrs;
			this.trimSec = (short)sec;
			this.trimNano = 0;
			this.nanoFactor = nanoF;
		}
		private Resolution(int numSubMilli, String format, int nano, double nanoF) {
			this.format = format;
			this.numSubMilli = numSubMilli;
			this.trimHours = 1;
			this.trimSec = 1;
			this.trimNano = nano;
			this.nanoFactor = nanoF;
		}
		public int floorHours(int julianH) {
			return MathUtil.floorToMultiple(julianH, trimHours);
		}
		public short floorSeconds(short s) {
			return trimSec == 0 ? 0 : MathUtil.floorToMultiple(s, trimSec);
		}
		public int floorNanos(int n) {
			return trimNano == 0 ? 0 : MathUtil.floorToMultiple(n, trimNano);
		}
	}
	
	private Resolution resolution;
	private int julianHour;
	private short seconds;
	private int nanos;

	/**
	 * @deprecated GWT serialization only
	 */
	@Deprecated
	public TimeSpec() {}
	
	/**
	 * @param javaTimeMillis time in milliseconds (time-zone agnostic)
	 * @param nanos
	 */
	TimeSpec(long javaTimeMillis, int nanosInMilli, Resolution res) {
		final int h = (int)(javaTimeMillis / MILLIS_IN_HOUR) + JAVA_EPOCH_HOUR;
		final int millis = (int)(javaTimeMillis % MILLIS_IN_HOUR);
		final short s = (short)(millis / 1000);
		final int n = (millis % 1000) * 1000 * 1000 + nanosInMilli; 
		if (res == null) {
			res = calculateMaxResolution(h, s, n);
		}
		julianHour =  res.floorHours(h);
		seconds = res.floorSeconds(s); 
		nanos = res.floorNanos(n);
		resolution = res;
	}

	public TimeSpec(String specString) {
		final int len = specString.length();

		final int yearIdx = specString.indexOf('-',1);//allow initial minus
		int yearStrLen = yearIdx < 0 ? len : yearIdx;
		final int year = parseInt(specString.substring(specString.startsWith("+") ? 1 : 0, yearStrLen));

		CheckUtil.checkArgument(yearStrLen >= ((year > 0) ? 4 : 5), "Year should be at least 4 digits");
		CheckUtil.checkArgument(year <= MAX_YEAR, "Time too large; maximal year is "+MAX_YEAR); 
		CheckUtil.checkArgument(year >= MIN_YEAR, "Time too small; minimal year is "+MIN_YEAR);
		if (year > 9999) {
			CheckUtil.checkArgument(specString.startsWith("+"), "Years larger than 9999 should be prefixed with '+'");
		} else if (year < 0) {
			CheckUtil.checkArgument(yearStrLen >= 5, "Negative years should be padded with '0'");
		}
		if (yearIdx < 0) {
			resolution = YEAR;
			julianHour = toJulianHour(year, 1, 1, 0);
			seconds = 0;
			nanos = 0;
			return;
		}

		final int monthIdx = specString.indexOf('-', yearIdx + 1);
		final int month = parseInt(specString.substring(yearIdx + 1, monthIdx < 0 ? len : monthIdx));
		if (monthIdx < 0) {
			resolution = MONTH;
			julianHour = toJulianHour(year, month, 1, 0);
			seconds = 0;
			nanos = 0;
			return;
		}

		final int tIdx = specString.indexOf('T', monthIdx + 1);
		final int day = parseInt(specString.substring(monthIdx + 1, tIdx < 0 ? len : tIdx));
		if (tIdx < 0) {
			resolution = DAY;
			julianHour = toJulianDay(year, month, day) * 24;
			seconds = 0;
			nanos = 0;
			return;
		}

		final int hIdx = specString.indexOf(':', tIdx + 1);
		int hr = parseInt(specString.substring(tIdx + 1, hIdx < 0 ? len : hIdx));
		julianHour = toJulianHour(year, month, day, hr);
		if (hIdx < 0) {
			resolution = HOUR;
			seconds = 0;
			nanos = 0;
			return;
		}

		final int mIdx = specString.indexOf(':', hIdx + 1);
		short sec = (short)(parseShort(specString.substring(hIdx + 1, mIdx < 0 ? len : mIdx)) * 60);
		if (mIdx < 0) {
			resolution = MINUTE;
			seconds = sec;
			nanos = 0;
			return;
		}

		int sIdx = specString.indexOf('.', mIdx + 1);
		seconds = (short)(sec + parseShort(specString.substring(mIdx + 1, sIdx < 0 ? len : sIdx)));
		if (sIdx < 0) {
			resolution = SECOND;
			nanos = 0;
			return;
		}

		int fact = 100*1000*1000;
		int fCnt = 0;
		int ns = 0;
		while (++sIdx < specString.length()) {
			ns += fact * (specString.charAt(sIdx) - '0');
			fact/=10;
			fCnt++;
		}
		nanos = ns;
		resolution = fCnt > 6 ? NANOSECOND : fCnt > 3 ? MICROSECOND : MILLISECOND;
	}

	/**
	 * 
	 * @param date Date with embedded timezone information 
	 * @param nanos
	 * @param resolution
	 */
	public TimeSpec(Date date, int nanosInMilli, Resolution resolution) {
		this(stripLocalTimeZone(date), nanosInMilli, resolution);
	}

	@Override
	public String toString() {
		return toISOString();
	}
	
	/**
	 * yyyy-MM-dd'T'HH:mm:ss.SSS
	 * 
	 * @return
	 */
	public String toISOString() {
		int[] YMD = toGregorianYMD(julianHour / 24);
		StringBuilder sb = new StringBuilder();
		appendPadYear(sb, YMD[0]);
		if (resolution.compareTo(Resolution.MONTH) >= 0) appendPad(sb.append('-'), YMD[1]);
		if (resolution.compareTo(Resolution.DAY) >= 0) appendPad(sb.append('-'), YMD[2]);
		if (resolution.compareTo(Resolution.HOUR) >= 0) appendPad(sb.append('T'), mod(julianHour, 24));
		if (resolution.compareTo(Resolution.MINUTE) >= 0) appendPad(sb.append(':'), seconds / 60);
		if (resolution.compareTo(Resolution.SECOND) >= 0) appendPad(sb.append(':'), seconds % 60);
		if (resolution.compareTo(Resolution.MILLISECOND) >= 0) appendPad(sb.append('.'), nanos / 1000000, 3);
		if (resolution.compareTo(Resolution.MICROSECOND) >= 0) appendPad(sb, (nanos % 1000000) / 1000, 3);
		if (resolution.compareTo(Resolution.NANOSECOND) >= 0) appendPad(sb, nanos % 1000, 3);
		return sb.toString();
	}
	
	public int getYear() {
		return toGregorianYMD(divFloor(julianHour, 24))[0];
	}	

	public int getMonth() {
		return toGregorianYMD(divFloor(julianHour, 24))[1];
	}	

	public int getDayOfMonth() {
		return toGregorianYMD(divFloor(julianHour, 24))[2];
	}
	
	public int getHourInDay() {
		return divFloor(julianHour, 24);
	}
	
	public int getMinuteInHour() {
		return seconds/60;
	}
	
	public int getSecondInMinute() {
		return seconds % 60;
	}
	
	public int getMilliInSecond() {
		return nanos / 1000000;
	}
	
	public int getNanosInMilli() {
		return nanos % 1000000;
	}
	
	public TimeSpec setYear(int year) {
		StringBuilder sb = new StringBuilder();
		appendPadYear(sb, year);
		sb.append(toISOString().replaceAll("^[+-]?[0-9]+", ""));
		return new TimeSpec(sb.toString());
	}

	public TimeSpec setMonth(int month) {
		if (resolution.compareTo(Resolution.MONTH) < 0) {
			throw new IllegalStateException("Cannot set month on TimeSpec with resolution "+resolution);
		}
		StringBuilder sb = new StringBuilder();
		String cur = toISOString();
		int firstDash = cur.indexOf('-');
		sb.append(cur, 0, firstDash+1);
		appendPad(sb, month, 2);
		sb.append(cur, cur.indexOf('-', firstDash+1), cur.length());
		return new TimeSpec(sb.toString());
	}

	public TimeSpec setDayOfMonth(int day) {
		if (resolution.compareTo(Resolution.DAY) < 0) {
			throw new IllegalStateException("Cannot set day on TimeSpec with resolution "+resolution);
		}
		StringBuilder sb = new StringBuilder();
		String cur = toISOString();
		int dash2 = cur.indexOf('-', cur.indexOf('-')+1);
		sb.append(cur, 0, dash2+1);
		appendPad(sb, day, 2);
		int tIndex = cur.indexOf('T');
		if (tIndex > 0) {
			sb.append(cur, tIndex, cur.length());
		}
		return new TimeSpec(sb.toString());
	}

	@Override
	public String toCanonicalString() {
		return toISOString();
	}

	public Resolution getResolution() {
		return resolution;
	}
	
	public long toJavaTimeUtc() {
		return (long) MILLIS_IN_HOUR * (julianHour - JAVA_EPOCH_HOUR) + seconds * 1000 + (nanos + 500000)/1000000;
	}
	
	/**
	 * Caveat: This will produce a non-local timestamp offset by whatever the current machine time zone setting is. 
	 * 
	 * @return
	 */
	public long toJavaTimeWithJvmLocalTimeZoneOffset() {
		int[] ymd = toGregorianYMD(divFloor(julianHour, 24));
		int h = mod(julianHour, 24);
		int min = seconds/60;
		
		@SuppressWarnings("deprecation")
		Date temp = new Date(ymd[0]-1900, ymd[1] - 1, ymd[2], h, min, seconds % 60);
		return temp.getTime() + (nanos + 500000)/1000000;
	}

	@Override
	public int compareTo(TimeSpec o) {
		if (this.julianHour != o.julianHour) return this.julianHour - o.julianHour;
		if (this.seconds != o.seconds) return this.seconds - o.seconds;
		if (this.nanos != o.nanos) return this.nanos - o.nanos;
		return this.resolution.compareTo(o.resolution);
	}

	/**
	 * 
	 * @param b
	 * @param res
	 * @return this - b
	 */
	public double difference(TimeSpec b, Resolution res) {
		final double difH = julianHour - b.julianHour;
		final double difS = 3600*difH + (seconds - b.seconds);
		final double difN = 1e9*difS + (nanos - b.nanos);
		return difN / res.nanoFactor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + julianHour;
		result = prime * result + nanos;
		result = prime * result + ((resolution == null) ? 0 : resolution.hashCode());
		result = prime * result + seconds;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TimeSpec other = (TimeSpec)obj;
		if (julianHour != other.julianHour) {
			return false;
		}
		if (nanos != other.nanos) {
			return false;
		}
		if (resolution != other.resolution) {
			return false;
		}
		if (seconds != other.seconds) {
			return false;
		}
		return true;
	}

	public static final String toISOString(TimeSpec ts) {
		return toISOString(ts, null);
	}
	
	public static final String toISOString(TimeSpec ts, String ifNull) {
		return ts==null ? ifNull : ts.toISOString();
	}

	public static final int toJulianDay(int year, int month, int dayOfMonth) {
		final int a = divFloor(14 - month, 12);
		final int m = month + 12 * a - 3;
		final int y = year + 4800 - a;
		return dayOfMonth + divFloor(153 * m + 2, 5) + 365 * y + divFloor(y, 4) - divFloor(y, 100) + divFloor(y, 400) - 32045;
	}

	public static final int[] toGregorianYMD(int julianDay) {
		int f = julianDay + divFloor(3 * divFloor(4 * julianDay + 274277, 146097),4)  + 1363;
		int e = 4 * f + 3;
		int g = divFloor(mod(e, 1461), 4);
		int h = 5 * g + 2;
		int D = divFloor(mod(h, 153), 5) + 1;
		int M = mod(divFloor(h, 153) + 2, 12) + 1;
		int Y = divFloor(e, 1461) + divFloor(14 - M, 12)  - 4716;
		return new int[] {Y, M, D};
	}

	private static final int toJulianHour(int year, int month, int dayOfMonth, int hour) {
		return 24 * toJulianDay(year, month, dayOfMonth) + hour;
	}

	@SuppressWarnings("deprecation")
	private static long stripLocalTimeZone(Date date) {
		return date.getTime() - date.getTimezoneOffset()*60*1000;
	}

	private static Resolution calculateMaxResolution(final int h, final short s, final int n) {
		if (n > 0) {
			if (n % 1000000 > 0) {
				if (n % 1000 > 0) {
					return NANOSECOND;
				}
				return MICROSECOND;
			}
			return MILLISECOND;
		}
		if (s > 0) {
			if (s % 60 > 0) {
				return SECOND;
			}
			return MINUTE;
		}
		if (h % 24 > 0) {
			return HOUR;
		}
		return DAY;
	}

	private static StringBuilder appendPad(StringBuilder sb, int value) {
		return appendPad(sb, value, 2);
	}

	private static StringBuilder appendPad(StringBuilder sb, int value, int len) {
		return appendPadded(sb, value, len, '0', true);
	}
	
	private static StringBuilder appendPadYear(StringBuilder sb, int value) {
		if (value < 0) {
			return appendPad(sb.append('-'), -value, 4);
		}
		if (value == 0 || value >= 10000) {
			sb.append('+');
		}
		return appendPad(sb, value, 4);
	}

	public static TimeSpec createForCurrentDate() {
		return createForCurrentTime(DAY);
	}
	
	public static TimeSpec createForCurrentTime(Resolution resolution) {
		return createFor(new Date(), resolution);
	}
	
	public static TimeSpec createFor(Date d, Resolution resolution) {
		return d == null ? null : new TimeSpec(d, 0, resolution);
	}

	public static TimeSpec createFor(Date d, int nanos, Resolution resolution) {
		return d == null ? null : new TimeSpec(d, nanos, resolution);
	}

	public static TimeSpec createWithAutoResolution(Date d, int nanos) {
		return d == null ? null : new TimeSpec(d, nanos, null);
	}
	
	public static double duration(long tStart, long tEnd, Resolution res) {
		final double difN = (tEnd - tStart)*10e6;
		return difN/res.nanoFactor;
	}
	
	/**
	 * 
	 * @param d
	 * @param dUnits
	 * @param formatPattern
	 * @return
	 */
	public static String durationToString(double d, Resolution dUnits, String formatPattern) {
		if (d == 0) return "0";
		int sign = d > 0 ? 1 : -1;
		double dd = sign * d * dUnits.nanoFactor;
		
		Resolution[] res = Resolution.values();
		String[] units = new String[]{"y", "m", "w", "d", "h", "min", "s", "ms", "µs", "ns"};
		
		int[] ret = new int[res.length];
		for (int i = 0; i < ret.length; i++) {
			if (dd >= res[i].nanoFactor) {
				double val = dd / res[i].nanoFactor;
				return NumberFormatUtil.create(formatPattern).format(sign * val) + " " + units[i];
			}
		}
		return NumberFormatUtil.create(formatPattern).format(sign * dd) + " ns";
	}

	public static TimeSpec createForSqlTimestamp(Timestamp tsVal, Resolution res) {
		if (tsVal == null) {
			return null;
		}
		int nanos = tsVal.getNanos();
		int millis = nanos/1000000;
		int nanosInMilli = nanos % 1000000;
		return new TimeSpec(stripLocalTimeZone(tsVal) + millis, nanosInMilli, res);
	}
}
