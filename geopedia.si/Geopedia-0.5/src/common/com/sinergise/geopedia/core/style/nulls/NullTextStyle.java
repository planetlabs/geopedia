package com.sinergise.geopedia.core.style.nulls;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.TextStyleSpec;

public final class NullTextStyle extends TextStyleSpec
{
	public static final NullTextStyle instance = new NullTextStyle();

    public void toString(StringBuffer sb)
	{
		sb.append("null");
	}
    @Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(NullTextStyle.class+" not supported");	
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
		return new NullTextStyle();
	}
}
