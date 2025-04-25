package com.sinergise.geopedia.core.style.nulls;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.StyleSpec;

public final class NullStyle extends StyleSpec
{
	public void toString(StringBuffer sb)
	{
		sb.append("null");
	}
	
	@Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(NullStyle.class+" not supported");	
	}
	
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return true;
	}
	
	public Object clone()
	{
		return new NullStyle();
	}
	
	public static final NullStyle instance = new NullStyle();
}
