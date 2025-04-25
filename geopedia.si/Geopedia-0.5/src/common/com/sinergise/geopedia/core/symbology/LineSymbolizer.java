package com.sinergise.geopedia.core.symbology;

import java.awt.Color;

/**
 * Used for lines and polygons; order in Symbology determines which is rendered first
 */
public interface LineSymbolizer extends Symbolizer {
	
	public static final LineType DEFAULT_LINETYPE=LineType.SOLID;
	public static final Color DEFAULT_STROKE=new Color(0);
	public static final double DEFAULT_STROKEWIDTH=1;
	public static final double DEFAULT_OPACITY=1;
	public static final double DEFAULT_DISPLACEMENTX=0;
	public static final double DEFAULT_DISPLACEMENTY=0;
	
	
	enum LineType {NONE, SOLID, DOTS, DASHES, DOTS_DASHES}
	
	Color getStroke();
	double getStrokeWidth();
	
	LineType getLineType();

	//	enum LineJoin {MITER, ROUND, BEVEL}
	//LineJoin getStrokeLineJoin();
	
	// enum LineCap {BUTT, ROUND, SQUARE}
	//LineCap getStrokeLineCap();
	
	//double getStrokeMiterLimit();
	//double[] getStrokeDashArray();
	//double getStrokeDashOffset();
}
