package com.sinergise.geopedia.core.style.bools;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.BooleanSpec;

public final class Not extends BooleanSpec
{
	public BooleanSpec base;
	
	public Not()
	{
		// ...
	}
	
	public Not(BooleanSpec base)
	{
		this.base = base;
	}
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(Not.class+" not supported");	
	}

	
	public void toString(StringBuffer sb)
	{
		sb.append("!(");
		base.toString(sb);
		sb.append(')');
	}

	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			base.accept(v);
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return base.isConst();
	}
	
	public Object clone()
	{
		return new Not((BooleanSpec)base.clone());
	}
}
