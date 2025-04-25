package com.sinergise.geopedia.core.style.model;

import com.sinergise.geopedia.core.style.StyleVisitor;


public final class NullSpec extends StyleSpecPart
{
	public final int getType()
	{
		return T_NULL;
	}
	
	public NullSpec() 
	{
		// damn serialization
	}
	
	public static final NullSpec _instance = new NullSpec();
	
	public void toString(StringBuffer sb)
	{
		sb.append("null");
	}
	
	@Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(NullSpec.class+" not supported");	
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
		return new NullSpec();
	}
}
