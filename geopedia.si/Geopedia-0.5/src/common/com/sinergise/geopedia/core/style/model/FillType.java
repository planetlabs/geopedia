package com.sinergise.geopedia.core.style.model;

public abstract class FillType
{
	private FillType() { /**/ }
	
	public static final int MIN_VALID_ID = 0;
	public static final int MAX_VALID_ID = 9;
	
	public static final int NONE = 0;
	
	public static final int SOLID = 1;
	
	/** ///// */
	public static final int SLASHES = 2;
	
	/** \\\\\ */
	public static final int BACKSLASHES = 3;
	
	/** ----- */
	public static final int HOR_LINES = 4;
	
	/** ||||| */
	public static final int VER_LINES = 5;
	
	/** ..... */
	public static final int DOTS = 6;
	
	/** ##### */
	public static final int GRID = 7;
	
	/** XXXXX */
	public static final int DIAG_GRID = 8;
	
	/** ^^^^^ */
	public static final int TRIANGLES = 9;
	
	public static final Integer[] ids =      { NONE, SOLID, SLASHES, BACKSLASHES, HOR_LINES, VER_LINES, DOTS, GRID, DIAG_GRID, TRIANGLES };
	public static final String[] names = { "none", "solid", "slashes", "backslashes", "horLines", "verLines", "dots", "grid", "diagGrid", "triangles" };
	
	public static final int idForName(String name) {
		for (int i = 0; i < names.length; i++) {
	        if (names[i].equals(name)) return ids[i];
        }
		return Integer.MIN_VALUE;
	}
}
