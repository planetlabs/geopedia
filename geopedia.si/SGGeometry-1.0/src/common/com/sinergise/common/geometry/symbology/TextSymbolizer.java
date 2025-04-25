package com.sinergise.common.geometry.symbology;

import com.sinergise.common.util.math.SGColor;

public interface TextSymbolizer extends Symbolizer {
	public static final double DEFAULT_DISPLACEMENTX = 0;
	public static final double DEFAULT_DISPLACEMENTY = 0;
	public static final double DEFAULT_OPACITY = 1;
	public static final String DEFAULT_LABEL = null;
	public static final SGColor DEFAULT_FILLCOLOR = SGColor.BLACK;

	String getLabel();
	SymbolizerFont getFont();

	//SymbolizerLabelPlacement getLabelPlacement();
	//SymbolizerHalo getHalo();

	SGColor getFill(); //TODO: replace with FillSymbolizer
}
