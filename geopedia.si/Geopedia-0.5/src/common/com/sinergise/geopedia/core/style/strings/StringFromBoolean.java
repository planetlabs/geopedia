package com.sinergise.geopedia.core.style.strings;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.BooleanSpec;
import com.sinergise.geopedia.core.style.model.StringSpec;

public final class StringFromBoolean extends StringSpec
{
	public BooleanSpec val;
	
	public StringFromBoolean()
	{
		// ...
	}
	
	public StringFromBoolean(BooleanSpec ns)
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
		val.toStringJS(sb);
    }
	
	@Override
	public void toStringJS(StringBuffer sb) {
		val.toString(sb);
		
	}
	
	public Object clone()
	{
		return new StringFromBoolean((BooleanSpec)val.clone());
	}
}
