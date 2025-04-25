package com.sinergise.geopedia.core.style.nulls;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.FillStyleSpec;

public final class NullFillStyle extends FillStyleSpec
{
	public static final NullFillStyle instance = new NullFillStyle();

    public void toString(StringBuffer sb)
	{
		sb.append("null");
	}
	
    
    @Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(NullFillStyle.class+" not supported");	
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
		return new NullFillStyle();
	}
}
