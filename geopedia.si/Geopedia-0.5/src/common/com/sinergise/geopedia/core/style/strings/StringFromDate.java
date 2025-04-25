package com.sinergise.geopedia.core.style.strings;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.DateSpec;
import com.sinergise.geopedia.core.style.model.StringSpec;

public final class StringFromDate extends StringSpec
{
	public DateSpec val;
	
	public StringFromDate()
	{
		// ...
	}
	
	public StringFromDate(DateSpec ns)
	{
		this.val = ns;
	}
	
	public void accept(StyleVisitor v)
    {
		if (v.visit(this, true)) {
			val.accept(v);
			v.visit(this, false);
		}
    }

	public boolean isConst()
    {
		return val.isConst();
    }

	public void toString(StringBuffer sb)
    {
		val.toString(sb);
    }
	
	@Override
	public void toStringJS(StringBuffer sb) {
		val.toStringJS(sb);
	}
	
	
	public Object clone()
	{
		return new StringFromDate((DateSpec)val.clone());
	}
}
