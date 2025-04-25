package com.sinergise.geopedia.core.symbology;


public interface SymbolizerFont {
	
	public static final FontStyle DEFAULT_FONTSTYLE=FontStyle.NORMAL;
	public static final FontWeight DEFAULT_FONTWEIGHT=FontWeight.BOLD;
	public static final String DEFAULT_FONTFAMILY="SansSerif";
	public static final double DEFAULT_FONTSIZE=12.0;
	
	enum FontStyle {NORMAL, ITALIC} //, OBLIQUE}
	enum FontWeight{NORMAL, BOLD} //, BOLDER, LIGHTER, W100, W200, W300, W400, W500, W600, W700, W800, W900

	String getFontFamily();
	double getFontSize();
	FontStyle getFontStyle();
	FontWeight getFontWeight();
}
