package com.sinergise.geopedia.core.style.model;

public abstract class SymbolId {
	private SymbolId() { /**/
	}
	
	public static final int NONE = 0;
	public static final int STAR5 = 1;
	public static final int CROSS = 2;
	public static final int SQUARE = 3;
	public static final int DIAG_SQUARE = 4;
	public static final int CIRCLE = 5;
	public static final int PLUS = 6;
	public static final int TRIANGLE = 7;
	public static final int HOUSE = 8;
	public static final int PENTAGON = 9;
	public static final int HEXAGON = 10;
	public static final int STAR4 = 11;
	public static final int STAR6 = 12;
	public static final int CHURCH = 13;
	
	public static final int[] ids = new int[] {
		NONE,STAR5,CROSS,SQUARE,DIAG_SQUARE,CIRCLE,PLUS,TRIANGLE,HOUSE,PENTAGON,HEXAGON,STAR4,STAR6,CHURCH
	};
	
	public static final String[] names = new String[ids.length];
	static {
		names[NONE] = "none";
		names[STAR5] = "star5";
		names[CROSS] = "cross";
		names[SQUARE] = "square";
		names[DIAG_SQUARE] = "diagSquare";
		names[CIRCLE] = "circle";
		names[PLUS] = "plus";
		names[TRIANGLE] = "triangle";
		names[HOUSE] = "house";
		names[PENTAGON] = "pentagon";
		names[HEXAGON] = "hexagon";
		names[STAR4] = "star4";
		names[STAR6] = "star6";
		names[CHURCH] = "church";
	};
	
	public static final int MIN_VALID_ID = 0;
	
	public static final int MIN_SIZE = 4;

//	public static final int MAX_SIZE = 32;
	public static final int MAX_SIZE = 128;
	
	public static final int HIGHLIGHTED_POINT_SIZE = 32;
}
