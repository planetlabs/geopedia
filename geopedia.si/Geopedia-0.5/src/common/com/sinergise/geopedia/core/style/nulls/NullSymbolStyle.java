package com.sinergise.geopedia.core.style.nulls;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.SymbolStyleSpec;

public final class NullSymbolStyle extends SymbolStyleSpec
{
	public static final NullSymbolStyle instance=new NullSymbolStyle();

    public void toString(StringBuffer sb)
	{
		sb.append("null");
	}
    
    @Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(NullSymbolStyle.class+" not supported");	
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
		return new NullSymbolStyle();
	}
}
