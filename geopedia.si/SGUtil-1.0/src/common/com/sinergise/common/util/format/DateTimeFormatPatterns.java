package com.sinergise.common.util.format;

import java.util.HashMap;

public class DateTimeFormatPatterns {
	private static PatternCollection defaultCollection = new PatternCollection();
	
	protected static class PatternCollection {
		public PatternCollection() {
		}
		public HashMap<Locale, FormatPatterns> dateFormatPatterns = new HashMap<Locale, FormatPatterns>();
		public HashMap<Locale, FormatPatterns> timeFormatPatterns = new HashMap<Locale, FormatPatterns>();
		public HashMap<Locale, FormatPatterns> dateTimeFormatPatterns = new HashMap<Locale, FormatPatterns>();
	}
	
	
	public static final int SIZE_LONG = 2;
	public static final int SIZE_MEDIUM = 1;
	public static final int SIZE_SHORT = 0;
	
	public static final int TIME = 0;
	public static final int DATE = 1 ;
	public static final int DATE_TIME =2;
	
	
	static {
		addPatterns(DATE, Locale.SI, "d.M.y", "d.M.yyyy","EEEE, d MMMM yyyy");
		addPatterns(TIME, Locale.SI, "H:mm","H:mm:ss","H:mm:ss z");
		addPatterns(DATE_TIME, Locale.SI, "d. M. y H:mm", "d. M. yyyy H:mm:ss", "EEEE, d MMMM yyyy H:mm:ss z");
		addPatterns(DATE, Locale.HR, "d.M.y.", "d.M.yyyy.","EEEE, d MMMM yyyy.");
		addPatterns(TIME, Locale.HR, "H:mm","H:mm:ss","H:mm:ss z");
		addPatterns(DATE, Locale.EN, "M/d/yy", "MMM d, yyyy", "MMMM d, yyyy");
		addPatterns(TIME, Locale.EN, "h:mm a", "h:mm:ss a", "h:mm:ss a z");
		addPatterns(DATE, Locale.MK, "d.M.y.", "d.M.yyyy.","EEEE, d MMMM yyyy.");
		addPatterns(TIME, Locale.MK, "H:mm","H:mm:ss","H:mm:ss z");
		addPatterns(DATE, Locale.MU, "M/d/yy", "MMM d, yyyy", "MMMM d, yyyy");
		addPatterns(TIME, Locale.MU, "h:mm a", "h:mm:ss a", "h:mm:ss a z");
	}
	
	protected static class FormatPatterns {		
		public FormatPatterns(String sP, String mP, String lP) {
			shortPattern = sP;
			mediumPattern = mP;
			longPattern = lP;
		}
		public String longPattern;
		public String mediumPattern;
		public String shortPattern;
		
		public String getPattern (int size) {
			switch (size) {
				case SIZE_LONG:
					return longPattern;
				case SIZE_MEDIUM:
					return mediumPattern;
				case SIZE_SHORT:
					return shortPattern;
			}
			return mediumPattern;
		}
	}

	public static String getFormatPatternString(Locale locale, int type, int size) {
		return getFormatPatternString(defaultCollection, locale, type, size);
	}
	protected static String getFormatPatternString(PatternCollection collection, Locale locale, int type, int size) {
		FormatPatterns pat;
		switch (type) {
			case DATE:
				pat = collection.dateFormatPatterns.get(locale);
				break;
			case TIME:
				pat = collection.timeFormatPatterns.get(locale);
				break;
			case DATE_TIME:
				pat = collection.dateTimeFormatPatterns.get(locale);
				break;
			default:
				throw new RuntimeException("Unsupported type!");
		}		
		if (pat==null)
			return null;
		return pat.getPattern(size);
	}
	
	protected static void addPatterns(PatternCollection collection, int type, Locale locale, String sPattern, String mPattern, String lPattern) {
		FormatPatterns pat = new FormatPatterns(sPattern,mPattern, lPattern);
		switch (type) {
			case DATE:
				collection.dateFormatPatterns.put(locale, pat);
				break;
			case TIME:
				collection.timeFormatPatterns.put(locale, pat);
				break;
			case DATE_TIME:
				collection.dateTimeFormatPatterns.put(locale, pat);
				break;
			default:
				throw new RuntimeException("Unsupported type!");
		}
	}
	private static void addPatterns(int type, Locale locale, String sPattern, String mPattern, String lPattern) {
		addPatterns(defaultCollection,type,locale, sPattern,mPattern,lPattern);
	}
	}
