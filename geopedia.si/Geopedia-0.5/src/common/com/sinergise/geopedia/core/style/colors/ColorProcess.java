package com.sinergise.geopedia.core.style.colors;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.ColorSpec;

public final class ColorProcess extends ColorSpec
{
	/** r,g,b = 0.3*r + 0.59*g + 0.11*b */
	public static final int T_GRAYSCALE = 1;

	/** r = 255-r ... */
	public static final int T_INVERT = 2;

	/** r = r^128 ... */
	public static final int T_SHIFT = 3;

	/** r = r+0.5*(255-r) ... */
	public static final int T_BRIGHTER = 4;

	/** r = 0.5*r ... */
	public static final int T_DARKER = 5;

	public int type;
	public ColorSpec base;

	public ColorProcess()
	{
		//
	}

	public ColorProcess(int type, ColorSpec base)
	{
		this.type = type;
		this.base = base;
	}

	
    @Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(ColorProcess.class+" not supported");	
	}

	public void toString(StringBuffer sb)
	{
		sb.append("(");
		base.toString(sb);

		switch (type) {
		case T_GRAYSCALE:
			sb.append(").grayscale");
			break;
		case T_INVERT:
			sb.append(").invert");
			break;
		case T_SHIFT:
			sb.append(").shift");
			break;
		case T_BRIGHTER:
			sb.append(").brighter");
			break;
		case T_DARKER:
			sb.append(").darker");
			break;
		default:
			throw new IllegalStateException();
		}
	}

	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			base.accept(v);
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return base.isConst();
	}
	
	public Object clone()
	{
		return new ColorProcess(type, (ColorSpec) base.clone());
	}
}
