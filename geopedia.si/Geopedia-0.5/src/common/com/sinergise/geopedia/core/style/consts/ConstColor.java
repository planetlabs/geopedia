package com.sinergise.geopedia.core.style.consts;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.ColorSpec;

public final class ConstColor extends ColorSpec
{
	public int argb;

	public ConstColor()
	{
		// ...
	}

	public ConstColor(int argb)
	{
		this.argb = argb;
	}

	public void toString(StringBuffer sb)
	{
		sb.append("argb(0x");
		sb.append(Integer.toHexString(argb));
		sb.append(")");
	}
	
	public void toStringJS(StringBuffer sb)
	{
		sb.append("0x");
		sb.append(Integer.toHexString(argb));
	}

	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true))
			v.visit(this, false);
	}
	
	public boolean isConst()
	{
		return true;
	}
	
	public Object clone()
	{
		return new ConstColor(argb);
	}
}
