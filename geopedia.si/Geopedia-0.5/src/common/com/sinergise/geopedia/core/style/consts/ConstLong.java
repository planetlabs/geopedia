package com.sinergise.geopedia.core.style.consts;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.NumberSpec;

public final class ConstLong extends NumberSpec
{
	public long value;
	
	public ConstLong()
	{
		// ...
	}
	
	public ConstLong(long value)
	{
		this.value = value;
	}
	
	public void toString(StringBuffer sb)
	{
		sb.append(value);
		if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE)
			sb.append('L');
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
		return new ConstLong(value);
	}
}
