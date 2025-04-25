package com.sinergise.geopedia.core.style.model;


public abstract class FontId
{
	private FontId() { /**/ }
	
	public static final int MIN_VALID_ID = 0;
	public static final int MAX_VALID_ID = 1;
	
	public static final int NONE = 0;
	public static final int DEFAULT = 1;
	
	public static final int MIN_SIZE = 5;
	public static final int MAX_SIZE = 20;
	
	public static final int[] ids = { NONE, DEFAULT };
	public static final String[] names = { "none", "default" };
}
