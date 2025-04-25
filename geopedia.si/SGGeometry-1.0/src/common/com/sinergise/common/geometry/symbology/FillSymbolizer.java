package com.sinergise.common.geometry.symbology;

import com.sinergise.common.util.math.SGColor;

public interface FillSymbolizer extends Symbolizer {
	enum GPFillType {
		/**
		 * @deprecated no fill symbolizer should indicate absence of fill rendering...
		 */
		@Deprecated
		NONE(0), 
		SOLID(1), SLASHES(2), BACKSLASHES(3), HOR_LINES(4), VER_LINES(5), DOTS(6), GRID(7), DIAG_GRID(8), TRIANGLES(9);
		private int id;
		private GPFillType(int id) {
			this.id=id;
		}
		
		public int getId() {
			return id;
		}
	}

	public static final GPFillType DEFAULT_FILLTYPE = GPFillType.SOLID;
	public static final SGColor DEFAULT_FILL = SGColor.BLACK;
	public static final SGColor DEFAULT_FILLBACKGROUND = SGColor.BLACK;
	public static final double DEFAULT_OPACITY = 1;
	public static final double DEFAULT_DISPLACEMENTX = 0;
	public static final double DEFAULT_DISPLACEMENTY = 0;


	GPFillType getFillType();
	SGColor getFill();
	SGColor getFillBackground();	
}
