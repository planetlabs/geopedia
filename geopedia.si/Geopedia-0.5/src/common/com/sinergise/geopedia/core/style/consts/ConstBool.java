package com.sinergise.geopedia.core.style.consts;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.BooleanSpec;

public final class ConstBool extends BooleanSpec
{
	public boolean value;

	public ConstBool()
	{
		this(false);
	}

	public ConstBool(boolean value)
	{
		this.value = value;
	}

	public void toString(StringBuffer sb)
	{
		sb.append(value ? "true" : "false");
	}
	
	public void toStringJS(StringBuffer sb)
	{
		sb.append(value ? "true" : "false");
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
		return new ConstBool(value);
	}
}
