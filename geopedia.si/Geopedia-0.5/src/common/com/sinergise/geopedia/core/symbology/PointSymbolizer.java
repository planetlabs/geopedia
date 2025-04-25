package com.sinergise.geopedia.core.symbology;

import java.awt.Color;

/**
 * Used for points; centroid should be used for polygons if PointSymbolizer is present in a polygon style
 * 
 * @author Miha
 */
public interface PointSymbolizer extends Symbolizer {
	
	public static final Color DEFAULT_FILL=new Color(0);
	public static final double DEFAULT_SIZE=14;
	public static final double DEFAULT_OPACITY=1;
	public static final int DEFAULT_SYMBOLID=1;
	public static final double DEFAULT_DISPLACEMENTX=0;
	public static final double DEFAULT_DISPLACEMENTY=0;
	
	
	double getSize();
//	double getRotation(); //clockwise decimal degrees
//  Point getAnchorPoint();
	
	Color getFill(); // replace with FillSymbolizer
	int getSymbolId();

//	SymbolizerFill getFill();
//	SymbolizerStroke getStroke();	
}
