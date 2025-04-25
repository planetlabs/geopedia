package com.sinergise.geopedia.core.entities.utils;

public interface EntityConsts 
{
	public static String PREFIX_THEME = "T";
	public static String PREFIX_LAYER = "L";
	public static String PREFIX_FEATURE = "F";
	public static String PREFIX_CATEGORY = "C";
	public static String PREFIX_DIRECTIONS = "D";
	public static Object PREFIX_THEMETABLE = "G";
	
	
	
	public static final String PARAM_WGS48_COORDINATES="c";
	public static final String PARAM_X = "x";
	public static final String PARAM_Y = "y";
	public static final String PARAM_SCALE = "s";
	public static final String PARAM_BASELAYERS = "b";
	public static final String PARAM_ADDRESS = "n";
	public static final String PARAM_UISETTINGS="o";
	
	// Keep these in order - more info => larger number
    public static final int COPY_BASIC = 1;
    public static final int COPY_META = 2;
    public static final int COPY_SHALLOW = 3;
    
    public enum DataScope {
    	BASIC, MEDIUM, ALL
    };
    
    
	public static final char URL_ITEMS_SEPARATOR = '_';
	public static final String SEPARATOR_FEATURE_TABLE=":";
	public static final String BASELAYERS_SEPARATOR = "-";
	public static final String DEST_PARAM_FEATURES = "f";
	public static final String DEST_PARAM_TABLES = "t";
	public static final String DEST_PARAM_POINTS = "p";
	public static final String DIR_PARAM_OPT_AH = "ah"; //directions option - avoid highways
	public static final String DESTINATIONS_SEPARATOR = "-";
	public static final String PARAM_DISPLAY_TAB = "v";
}
