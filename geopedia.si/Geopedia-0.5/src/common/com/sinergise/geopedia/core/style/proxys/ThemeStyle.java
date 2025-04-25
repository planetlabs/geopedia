package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.StyleSpec;

public final class ThemeStyle extends StyleSpec
{
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true))
			v.visit(this, false);
	}
	
	public boolean isConst()
	{
		return false; // TODO
	}
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(ThemeStyle.class+" not supported");	
	}

	public void toString(StringBuffer sb)
	{
		sb.append("$theme");
	}
	
	public Object clone()
	{
		return new ThemeStyle();
	}
}
