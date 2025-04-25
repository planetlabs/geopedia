package com.sinergise.geopedia.core.symbology;

import java.awt.Color;

public interface FillSymbolizer extends Symbolizer {
	enum GPFillType {NONE, SOLID, SLASHES, BACKSLASHES, HOR_LINES, VER_LINES, DOTS, GRID, DIAG_GRID, TRIANGLES}
	public static final GPFillType DEFAULT_FILLTYPE=GPFillType.SOLID;
	public static final Color DEFAULT_FILL=new Color(0);
	public static final Color DEFAULT_FILLBACKGROUND=new Color(0);
	public static final double DEFAULT_OPACITY=1;
	public static final double DEFAULT_DISPLACEMENTX=0;
	public static final double DEFAULT_DISPLACEMENTY=0;
	

	GPFillType getFillType();
	Color getFill();
	Color getFillBackground();	
}
