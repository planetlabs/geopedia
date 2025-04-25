package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.FillStyleSpec;
import com.sinergise.geopedia.core.style.model.StyleSpec;

public final class FillStyleOf extends FillStyleSpec
{
	public StyleSpec style;
	
	public FillStyleOf()
	{
		// ...
	}
	
	public FillStyleOf(StyleSpec style)
	{
		this.style = style;
	}
	
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(FillStyleOf.class+" not supported");	
	}

	public void toString(StringBuffer sb)
	{
		sb.append('(');
		style.toString(sb);
		sb.append(").fillStyle");
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			style.accept(v);
			
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return style.isConst();
	}
	
	public Object clone()
	{
		return new FillStyleOf((StyleSpec) style.clone());
	}
}
