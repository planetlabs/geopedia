package com.sinergise.geopedia.core.style.model;

import java.io.Serializable;

import com.sinergise.geopedia.core.style.StyleVisitor;

public abstract class StyleSpecPart implements Serializable, Cloneable
{

	private static final long serialVersionUID = 1L;

	/** whole style (i.e. it's a StyleSpec) */
	public static final int T_STYLE = 1;
	
	/** line style (i.e. it's a LineStyleSpec) */
	public static final int T_LINE_STYLE = 2;
	
	/** fill style (i.e. it's a FillStyleSpec) */
	public static final int T_FILL_STYLE = 3;
	
	/** symbol style (i.e. it's a SymbolStyleSpec) */
	public static final int T_SYMBOL_STYLE = 4;
	
	/** text style (i.e. it's a TextStyleSpec) */
	public static final int T_TEXT_STYLE = 5;
	
	/** color (i.e. it's a ColorSpec) */
	public static final int T_COLOR = 6;
	
	/** number (i.e. it's a NumberSpec) */
	public static final int T_NUMBER = 7;
	
	/** boolean (i.e. it's a BooleanSpec) */
	public static final int T_BOOLEAN = 8;
	
	/** string (i.e. it's a StringSpec) */
	public static final int T_STRING = 9;
	
	/** date (i.e. it's a DateSpec) */
	public static final int T_DATE = 10;
	
	/** fill type (i.e. it's a FillTypeSpec) */
	public static final int T_FILL_TYPE = 11;
	
	/** FontIdSpec */
	public static final int T_FONT_ID = 12;
	
	/** LineTypeSpec */
	public static final int T_LINE_TYPE = 13;
	
	/** SymbolIdSpec */
	public static final int T_SYMBOL_ID = 14;
	
	/** Null */
	public static final int T_NULL = 15;
	
	public abstract int getType();
	
	public final boolean isType(int type, boolean nullAllowed)
	{
		int t = getType();
		return (t == type) || (nullAllowed && t == T_NULL);
	}
	
	public abstract void toString(StringBuffer sb);
	
	public abstract void toStringJS(StringBuffer sb);
	
	
	public final String toStringJS()
	{
		StringBuffer sb = new StringBuffer();
		toStringJS(sb);
		return sb.toString();
	}
	public final String toString()
	{
		StringBuffer sb = new StringBuffer();
		toString(sb);
		return sb.toString();
	}
	
	public abstract void accept(StyleVisitor v);
	
	public abstract boolean isConst();
	
	public Object clone()
	{
		throw new IllegalStateException("Missing clone() for "+this);
	}
}
