package com.sinergise.geopedia.core.style.nulls;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.LineStyleSpec;

public final class NullLineStyle extends LineStyleSpec
{
	public static final NullLineStyle instance = new NullLineStyle();

    public void toString(StringBuffer sb)
	{
		sb.append("null");
	}
	
    @Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(NullLineStyle.class+" not supported");	
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
		return new NullLineStyle();
	}
}
