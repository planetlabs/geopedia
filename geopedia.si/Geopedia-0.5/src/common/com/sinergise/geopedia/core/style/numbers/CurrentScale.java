package com.sinergise.geopedia.core.style.numbers;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.NumberSpec;

public final class CurrentScale extends NumberSpec
{
	public void accept(StyleVisitor v)
    {
		if (v.visit(this, true))
			v.visit(this, false);
    }

	public boolean isConst()
    {
		return true;
    }

	public void toString(StringBuffer sb)
    {
		sb.append("$scale");
    }
	@Override
	public void toStringJS(StringBuffer sb) {
		sb.append("scale");
	}
	
	public Object clone()
	{
		return new CurrentScale();
	}
}