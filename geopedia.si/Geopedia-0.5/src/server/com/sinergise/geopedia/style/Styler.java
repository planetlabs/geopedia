package com.sinergise.geopedia.style;

import java.sql.SQLException;

public abstract class Styler
{
	public static final int CONST_FILL_BGCOLOR = 1 << 0;
	public static final int CONST_FILL_FGCOLOR = 1 << 1;
	public static final int CONST_FILL_TYPE    = 1 << 2;
	public static final int CONST_LINE_COLOR   = 1 << 3;
	public static final int CONST_LINE_TYPE    = 1 << 4;
	public static final int CONST_LINE_WIDTH   = 1 << 5; 
	public static final int CONST_FONT_ID      = 1 << 6;
	public static final int CONST_FONT_COLOR   = 1 << 7;
	public static final int CONST_FONT_ITALIC  = 1 << 8;
	public static final int CONST_FONT_BOLD    = 1 << 9;
	public static final int CONST_FONT_HEIGHT  = 1 << 10;
	public static final int CONST_SYM_ID       = 1 << 11;
	public static final int CONST_SYM_TEXT     = 1 << 12;
	public static final int CONST_SYM_SIZE     = 1 << 13;
	public static final int CONST_SYM_COLOR    = 1 << 14;
	
	public static final int CONST_EVERYTHING   = (1 << 15) - 1;
	
	public abstract int getConstParts();
	
	public abstract void preprocessRow() throws SQLException;
	
	/**
	 * Returns one of LineStyle constants, specifying the type of line to be
	 * drawn
	 */
	public abstract int getLineType();

	/** Returns line color in 8-bit ARGB format */
	public abstract int getLineColor();

	/** Returns line width in pixels */
	public abstract double getLineWidth();

	/**
	 * Returns one of FillStyle constants, specifying the type of fill to be
	 * drawn
	 */
	public abstract int getFillType();

	/**
	 * Returns background fill color in 8-bit ARGB format (or color for solid,
	 * if type == SOLID)
	 */
	public abstract int getBackFillColor();

	/** Returns foreground fill color in 8-bit ARGB format */
	public abstract int getForeFillColor();

	/**
	 * Returns standard symbol ID from SymbolIds.
	 */
	public abstract int getSymbolId();

	/** return color for symbols */
	public abstract int getSymbolColor();

	/** Returns text color in 8-bit ARGB format */
	public abstract int getTextColor();

	/** Returns font size in pixels */
	public abstract int getFontSize();

	/** Returns font ID */
	public abstract int getFontID();

	/** Returns text for display next to the symbol */
	public abstract String getSymbolText();
	
	/** Returns symbol size in pixels */
	public abstract int getSymbolSize();
	
	/** Returns whether symbol text should be bold */
	public abstract boolean getBold();
	
	/** Returns whether symbol text should be italic */
	public abstract boolean getItalic();
}
