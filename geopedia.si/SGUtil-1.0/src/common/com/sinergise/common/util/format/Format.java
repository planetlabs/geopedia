package com.sinergise.common.util.format;

import java.util.Date;

import com.sinergise.common.util.format.NumberFormatProvider.NumberFormatConstants;
import com.sinergise.common.util.lang.number.SGNumber;
import com.sinergise.common.util.string.StringUtil;

public abstract class Format {
	public static class SimpleDecimalFormatter implements NumberFormatter {
		private boolean truncDecimalZeros = true;
		private int numDec = 2;
		private char decSep = NumberFormatUtil.getDefaultConstants().decimalSeparator.charAt(0);
		private char thousandsSep = NumberFormatUtil.getDefaultConstants().groupingSeparator.charAt(0);
		private boolean useGrouping = false;

		@Override
		public String format(SGNumber number) {
			//TODO: Do this properly, without rounding
			return format(SGNumber.Util.asBigDecimal(number).doubleValue());
		}
		
		@Override
		public String format(double d) {
			if (Double.isNaN(d)) return "/";
			boolean neg = d < 0;
			if (neg) d = -d;
			final long lng = Math.round(d * Math.pow(10, numDec));
			if (lng == 0) {
				if (truncDecimalZeros || numDec<=0) return "0";
				neg = false;
			}
			final String lngStr = String.valueOf(lng);
			final StringBuffer all = new StringBuffer();

			if (numDec == 0) {
				all.append(lng);
			} else if (numDec < 0) {
				all.append(lngStr);
				for (int i = 0; i < -numDec; i++)
					all.append('0');
			} else {
				while (all.length() + lngStr.length() <= numDec) {
					all.append('0');
				}
				all.append(lngStr);
				all.insert(all.length() - numDec, decSep);
				if (truncDecimalZeros) {
					while (all.charAt(all.length() - 1) == '0') {
						all.deleteCharAt(all.length() - 1);
					}
					if (all.charAt(all.length() - 1) == decSep) {
						all.deleteCharAt(all.length() - 1);
					}
				}
			}
			if (useGrouping) {
				int decPos = all.indexOf(String.valueOf(decSep));
				if (decPos < 0) decPos = all.length();
				int thPos = decPos - 3;
				while (thPos > 0) {
					all.insert(thPos, thousandsSep);
					thPos -= 3;
				}
			}
			return neg ? "-" + all.toString() : all.toString();
		}

		public void setTruncDecimalZeros(boolean truncDecimalZeros) {
			this.truncDecimalZeros = truncDecimalZeros;
		}

		public SimpleDecimalFormatter setNumDec(int numDec) {
			this.numDec = numDec;
			return this;
		}

		public void setNumberFormatConsts(NumberFormatConstants consts) {
			this.decSep = consts.decimalSeparator.charAt(0);
			this.thousandsSep = consts.groupingSeparator.charAt(0);
		}
		
		public void setUseGrouping(boolean useGrouping) {
			this.useGrouping = useGrouping;
		}
	}
	
	
	public static class SimpleUnitsFormatter extends SimpleDecimalFormatter {
		private final int[] powers;
		private final String[] units;
		
		public SimpleUnitsFormatter(int[] powers, String[] units) {
			this.powers = powers;
			this.units = units;
		}

		@Override
		public String format(double d) {
			double unit = 0;
			double res = d;
			int i = 0;
			while (i < powers.length - 1) {
				unit = Math.pow(10, powers[i]);
				if (d > unit) {
					res = d / unit;
					break;
				}
				i++;
			}
			if (res == 0) return super.format(0.0);
			return super.format(res) + " " + units[i];
		}
	}
	
	public static class SimpleLengthFormatter extends SimpleUnitsFormatter {
		private static final int[] DEFAULT_LENGTH_POWERS = new int[]{3, 0, -2, -3, -6, -9};
		private static final String[] DEFAULT_LENGTH_UNITS = new String[]{"km", "m", "cm", "mm", "&mu;m", "nm"};
		public SimpleLengthFormatter() {
			this(DEFAULT_LENGTH_POWERS, DEFAULT_LENGTH_UNITS);
		}
		public SimpleLengthFormatter(int[] powers, String[] units) {
			super(powers, units);
		}
	}

	/**
	 * Outputs area in appropriate units (chosen from a pre-defined list or provided in constructor).
	 */
	public static class SimpleAreaFormatter extends SimpleUnitsFormatter {
		private static final int[] DEFAULT_AREA_POWERS = new int[]{6, 4, 2, 0, -2, -4, -6};
		private static final String[] DEFAULT_AREA_UNITS = new String[]{"km\u00b2", "ha", "a", "m\u00b2", "dm\u00b2", "cm\u00b2", "mm\u00b2"};

		public SimpleAreaFormatter() {
			this(DEFAULT_AREA_POWERS, DEFAULT_AREA_UNITS);
		}

		public SimpleAreaFormatter(int[] areaPowers, String[] areaUnits) {
			super(areaPowers, areaUnits);
		}
	}
	
	/**
	 * Pattern-based formatting of areas:
	 * <ul>
	 * <li><code>km</code> km<sup>2</sup> without the leading zero</li>
	 * <li><code>KM</code> km<sup>2</sup> with the leading zero</li>
	 * <li><code>ha</code> ha without the leading zero</li>
	 * <li><code>HA</code> ha with the leading zero</li>
	 * <li><code>aa</code> a without the leading zero</li>
	 * <li><code>AA</code> a with the leading zero</li>
	 * <li><code>mm</code> m2 without the leading zero</li>
	 * <li><code>MM</code> m2 with the leading zero</li>
	 * </ul>
	 */
	public static class PatternAreaFormatter implements NumberFormatter {
		private final NumberFormatConstants localeConsts;
		private final String pattern;
		
		public PatternAreaFormatter(NumberFormatConstants localeConsts, String pattern) {
			this.localeConsts = localeConsts;
			this.pattern = pattern;
		}
		
		public PatternAreaFormatter(String areaFormatPattern) {
			this(NumberFormatUtil.getDefaultConstants(), areaFormatPattern);
		}

		@Override
		public String format(double area) {
			if (!isAreaFormat(pattern)) {
				return NumberFormatUtil.create(pattern, localeConsts).format(area);
			}
			final long mRaw = Math.round(area);
			
			final int mVal = (int)(mRaw % 100);
			final int aVal = (int)((mRaw / 100) % 100);
			final int haVal = (int)((mRaw / 10000) % 100);
			final int kmVal = (int)((mRaw / 1000000) % 100);
			
			final String mStr = String.valueOf(mVal); 
			String mm = mVal < 10 && (aVal > 0 || haVal > 0 || kmVal  > 0) ? "0" + mStr : mStr;
			String MM = mVal < 10 ? "0" + mStr : mStr;
			final String aStr = String.valueOf(aVal);
			String aa = aVal < 10 && (haVal > 0 || kmVal > 0) ? "0" + aStr : aStr;
			String AA = aVal < 10 ? "0" + aStr : aStr;
			final String haStr = String.valueOf(haVal);
			String ha = haVal < 10 && kmVal > 0 ? "0" + haStr : haStr;
			String HA = haVal < 10 ? "0" + haStr : haStr;
			
			String km = String.valueOf(kmVal);
			String KM = kmVal < 10 ? "0" + km : km;
			
			String evaledPattern = pattern;
			if (kmVal == 0) evaledPattern = evaledPattern.replaceAll("km.", "");
			if (haVal == 0 && kmVal == 0) evaledPattern = evaledPattern.replaceAll("ha.", "");
			if (aVal == 0 && kmVal == 0 && haVal == 0)  evaledPattern = evaledPattern.replaceAll("aa.", "");
			
			if(mRaw >= 100000000 && (pattern.startsWith("km") || pattern.startsWith("KM"))) {
				km = String.valueOf(((mRaw / 1000000)));
				KM = km;
			} else if (mRaw >= 1000000 && (pattern.startsWith("ha") || pattern.startsWith("HA"))) {
				ha = String.valueOf(((mRaw / 10000)));
				HA = ha;
			} else if (mRaw >= 10000 && (pattern.startsWith("aa") || pattern.startsWith("AA"))) {
				aa = String.valueOf(((mRaw / 100)));
				AA = aa;
			} else if (mRaw >= 100 && (pattern.startsWith("mm") || pattern.startsWith("MM"))){
				mm = String.valueOf(mRaw);
				MM = mm;
			}
			
			return evaledPattern.replaceAll("mm", mm).replaceAll("MM", MM).replaceAll("aa", aa).replaceAll("AA", AA).replaceAll("ha", ha).replaceAll("HA", HA)
			              .replaceAll("km", km).replaceAll("KM", KM);
		}

		public static boolean isAreaFormat(String pattern) {
			return pattern.contains("mm") ||
			pattern.contains("MM") ||
			pattern.contains("aa") ||
			pattern.contains("AA") ||
			pattern.contains("ha") ||
			pattern.contains("HA") ||
			pattern.contains("km") ||
			pattern.contains("KM");
		}

		@Override
		public String format(SGNumber number) {
			//TODO: Do this properly without rounding
			return format(SGNumber.Util.asBigDecimal(number).doubleValue());
		}
	}
	
	
	
	public static final String        DEFAULT_DATETIME_FORMAT    = DateFormatUtil.getDefaultShortDateTimePattern();
	public static final String        DEFAULT_DATE_FORMAT        = DateFormatUtil.getDefaultShortDatePattern();
	public static final String        ISO_DATETIME_FORMAT        = DateFormatUtil.ISO_DATETIME_PATTERN;
	public static final String        ISO_DATE_FORMAT            = DateFormatUtil.ISO_DATE_PATTERN;
	
	public static final DateFormatter FORMATTER_DEFAULT_DATE     = DateFormatUtil.create(DEFAULT_DATE_FORMAT);
	public static final DateFormatter FORMATTER_DEFAULT_DATETIME = DateFormatUtil.create(DEFAULT_DATETIME_FORMAT);
	
	public static String fillISOZeros(final String isoNoZeroes) {
		int idx = isoNoZeroes.indexOf('-');
		if (idx < 0) {
			idx = isoNoZeroes.length();
		}
		final String year = isoNoZeroes.substring(0, idx);
		if (year.length() != 4) {
			throw new IllegalArgumentException("Year should be written with 4 digits (was: " + year + ")");
		}
		if (idx >= isoNoZeroes.length()) {
			return year;
		}
		
		final int mStart = idx + 1;
		idx = isoNoZeroes.indexOf('-', mStart);
		if (idx < 0) {
			idx = isoNoZeroes.length();
		}
		String month = isoNoZeroes.substring(mStart, idx);
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (month.length() != 2) {
			throw new IllegalArgumentException("Month should be written with 2 digits (was: " + month + ")");
		}
		if (idx >= isoNoZeroes.length()) {
			return year + "-" + month;
		}
		
		final int dStart = idx + 1;
		idx = isoNoZeroes.indexOf('-', dStart);
		if (idx < 0) {
			idx = isoNoZeroes.length();
		}
		String day = isoNoZeroes.substring(dStart, idx);
		if (day.length() == 1) {
			day = "0" + day;
		}
		if (day.length() != 2) {
			throw new IllegalArgumentException("Day should be written with 2 digits (was: " + day + ")");
		}
		return year + "-" + month + "-" + day;
	}
	
	public static String date(final Date date) {
		return FORMATTER_DEFAULT_DATE.formatDate(date);
	}
	
	public static String dateTime(final Date date) {
		return FORMATTER_DEFAULT_DATETIME.formatDate(date);
	}
	
	public static final char NULL_CHAR             = (char)0;
	public static final char DEFAULT_DEC_SEP       = NumberFormatUtil.getDefaultConstants().decimalSeparator.charAt(0);
	public static final char DEFAULT_THOUSANDS_SEP = NumberFormatUtil.getDefaultConstants().groupingSeparator.charAt(0);
	
	/**
	 * A generous reading of input values, which also takes into account possible thousands separators and different decimal separators
	 * 
	 * @param input
	 * @return
	 */
	public static final double readDecimal(final String input) {
		return readDecimal(input, DEFAULT_DEC_SEP, DEFAULT_THOUSANDS_SEP);
	}
	
	public static final double readDecimal(String input, final char decSep, final char thousandsSep) {
		input = input.trim();
		final int commaIdx = input.indexOf(decSep);
		final boolean commaTwice = input.indexOf(decSep, commaIdx + 1) > 0;
		int dotIdx = input.indexOf(thousandsSep);
		final boolean dotTwice = input.indexOf(thousandsSep, dotIdx + 1) > 0;
		
		if (commaTwice) {
			return Double.parseDouble(input.replaceAll("\\" + decSep, "").replaceAll("\\" + decSep, "."));
		} else if (dotTwice) {
			return Double.parseDouble(input.replaceAll("\\" + thousandsSep, "").replaceAll("\\" + decSep, "."));
		}
		if (commaIdx > 0) {
			if (dotIdx > commaIdx) { // dot after comma - treat dot as decimal
				return Double.parseDouble(input.replaceAll("\\" + decSep, "").replaceAll("\\" + thousandsSep, "."));
			} else if (dotIdx > 0) { // dot and comma
				return Double.parseDouble(input.replaceAll("\\" + thousandsSep, "").replaceAll("\\" + decSep, "."));
			} else { // only comma
				return Double.parseDouble(input.replaceAll("\\" + decSep, "."));
			}
		}
		dotIdx = input.lastIndexOf(thousandsSep);
		// Check if this is compliant with thousands separator
		// if (dotIdx == input.length() - 1 - 3)
		// return Double.parseDouble(input.replaceAll("\\" + thousandsSep,
		// ""));
		return Double.parseDouble(input.replaceAll("\\" + thousandsSep, "."));
	}
	
	public static final double readFirstDecimal(String input) {
		input = input.trim();
		int spcIdx = input.indexOf(' ');
		while (spcIdx > 0) {
			input = input.substring(0, spcIdx);
			spcIdx = input.indexOf(' ');
			if (spcIdx < 0) {
				spcIdx = input.indexOf('-', 1);
			}
		}
		System.err.println(input);
		return readDecimal(input);
	}
	
	/**
	 * format .00 urejen z java kodo namesto formaterjem ker java.text nemore na klienta..
	 * 
	 * @param d
	 * @return
	 */
	public static String format(final double d, final int numDec) {
		SimpleDecimalFormatter fmt = new SimpleDecimalFormatter();
		fmt.setNumDec(numDec);
		return fmt.format(d);
	}

	public static String format(double d, final int numDec, final char decSep, final char thousandsSep, final boolean truncDecimalZeros) {
		SimpleDecimalFormatter fmt = new SimpleDecimalFormatter();
		fmt.setNumDec(numDec);
		fmt.setTruncDecimalZeros(truncDecimalZeros);
		fmt.setNumberFormatConsts(new NumberFormatConstants(decSep, thousandsSep));
		fmt.setUseGrouping(thousandsSep != NULL_CHAR);
		return fmt.format(d);
	}
	
	public static String format(final double d, final int numDec, final boolean useThousandsSep) {
		SimpleDecimalFormatter fmt = new SimpleDecimalFormatter();
		fmt.setNumDec(numDec);
		fmt.setUseGrouping(useThousandsSep);
		return fmt.format(d);
	}
	
	public static String padWith(final String str, final char padChar, final int len, final boolean left) {
		return StringUtil.padWith(str, padChar, len, left);
	}
	
	public static String monthName(final int shownMonth) {
		return DateFormatUtil.getConstants().months()[shownMonth - 1];
	}
}
