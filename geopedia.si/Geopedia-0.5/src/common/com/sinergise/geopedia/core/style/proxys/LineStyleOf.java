package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.LineStyleSpec;
import com.sinergise.geopedia.core.style.model.StyleSpec;

public final class LineStyleOf extends LineStyleSpec
{
	public StyleSpec style;
	
	public LineStyleOf()
	{
		// ...
	}
	
	public LineStyleOf(StyleSpec style)
	{
		this.style = style;
	}
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(LineStyleOf.class+" not supported");	
	}

	public void toString(StringBuffer sb)
	{
		sb.append('(');
		style.toString(sb);
		sb.append(").lineStyle");
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
		return new LineStyleOf((StyleSpec) style.clone());
	}
}
