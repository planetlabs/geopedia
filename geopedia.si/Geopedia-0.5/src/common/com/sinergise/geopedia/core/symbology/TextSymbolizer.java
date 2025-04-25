package com.sinergise.geopedia.core.symbology;

import java.awt.Color;

public interface TextSymbolizer extends Symbolizer {
	public static final double DEFAULT_DISPLACEMENTX = 0;
	public static final double DEFAULT_DISPLACEMENTY = 0;
	public static final double DEFAULT_OPACITY = 1;
	public static final String DEFAULT_LABEL = null;
	public static final Color DEFAULT_FILLCOLOR = new Color(0);

	String getLabel();
	SymbolizerFont getFont();

	//SymbolizerLabelPlacement getLabelPlacement();
	//SymbolizerHalo getHalo();

	Color getFill(); //TODO: replace with FillSymbolizer
}
