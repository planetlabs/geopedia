package com.sinergise.geopedia.core.style.consts;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.NumberSpec;

public final class ConstDouble extends NumberSpec
{
	public double value;
	
	public ConstDouble()
	{
		// ...
	}
	
	public ConstDouble(double value)
	{
		this.value = value;
	}
	
	public void toString(StringBuffer sb)
	{
		sb.append(value);
	}
	
	
	public void toStringJS(StringBuffer sb)
	{
		sb.append(value);
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
		return new ConstDouble(value);
	}
}
