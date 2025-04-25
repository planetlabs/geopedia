package com.sinergise.common.geometry.symbology;


import com.sinergise.common.util.math.SGColor;

/**
 * Used for lines and polygons; order in Symbology determines which is rendered first
 */
public interface LineSymbolizer extends Symbolizer {
	
	public static final LineType DEFAULT_LINETYPE = LineType.SOLID;
	public static final SGColor DEFAULT_STROKE = SGColor.BLACK;
	public static final SGColor DEFAULT_STROKE_BACKGROUND = SGColor.TRANSPARENT;
	public static final double DEFAULT_STROKEWIDTH = 1;
	public static final double DEFAULT_OPACITY = 1;
	public static final double DEFAULT_DISPLACEMENTX = 0;
	public static final double DEFAULT_DISPLACEMENTY = 0;

	
	enum LineType {
		NONE(0), SOLID(1), DOTS(2), DASHES(3), DOTS_DASHES(4);
		
		private int id;
		private LineType(int id) {
			this.id=id;
		}
		public int getId() {
			return id;
		}
	}
	
	SGColor getStroke();
	SGColor getStrokeBackground();
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
