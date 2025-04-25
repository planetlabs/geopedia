package com.sinergise.java.util.format;

import com.sinergise.common.util.format.DateTimeFormatPatterns;
import com.sinergise.common.util.format.Locale;

public class JavaDateTimeFormatPatterns extends DateTimeFormatPatterns{
	public static final int COLLECTION_ORACLE = 0;
	public static final int COLLECTION_DEFAULT = -1;

	private static PatternCollection[] patCollections = new PatternCollection[1];

	
	private static final PatternCollection COL_ORACLE = getCollection(COLLECTION_ORACLE);

	private static PatternCollection getCollection(int collectionID) {
		if (collectionID<0 || collectionID>= patCollections.length) {
			throw new IllegalArgumentException("collectionID is out of range");
		}
		if (patCollections[collectionID]!=null)
			return  patCollections[collectionID];
		patCollections[collectionID]= new PatternCollection();
		return patCollections[collectionID];  
	}
	
	static {
		addPatterns(COL_ORACLE, DATE, Locale.SI, "DD.MM.YY", "DD.MM.YYYY","DAY, DD MONTH YYYY");
		addPatterns(COL_ORACLE, TIME, Locale.SI, "HH24:MI","HH24:MI:SS","HH24:MI:SS");
		addPatterns(COL_ORACLE, DATE, Locale.HR,  "DD.MM.YY.", "DD.MM.YYYY.","DAY, DD MONTH YYYY.");
		addPatterns(COL_ORACLE, TIME, Locale.HR, "HH24:MI","HH24:MI:SS","HH24:MI:SS");
		addPatterns(COL_ORACLE, DATE, Locale.EN, "MM/DD/YY", "MON DD, YYYY", "MONTH DD, YYYY");
		addPatterns(COL_ORACLE, TIME, Locale.EN, "HH12:MI AM", "HH12:MI:SS AM", "HH12:MI:SS AM");
	}
	
	
	
	public static String getFormatPatternString(int collectionID, Locale locale, int type, int size) {
		if (collectionID==COLLECTION_DEFAULT)
			return getFormatPatternString(locale, type, size);
		
		return getFormatPatternString(patCollections[collectionID], locale, type, size);
		
			
	}
}
