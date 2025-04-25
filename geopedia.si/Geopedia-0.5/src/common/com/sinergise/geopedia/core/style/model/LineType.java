package com.sinergise.geopedia.core.style.model;

public abstract class LineType
{
	private LineType() { /**/ }
	
	public static final int MIN_VALID_ID = 0;
	public static final int MAX_VALID_ID = 4;
	
	public static final int NONE = 0;
	public static final int SOLID = 1;
	public static final int DOTS = 2;
	public static final int DASHES = 3;
	public static final int DOTS_DASHES = 4;
	
	public static final int[] ids = { NONE, SOLID, DOTS, DASHES, DOTS_DASHES };
	public static final String[] names = { "none", "solid", "dots", "dashes", "dotsDashes" };
	
	public static final double MIN_WIDTH = 0.1;
	public static final double MAX_WIDTH = 16;
}
