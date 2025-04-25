package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.LineStyleSpec;
import com.sinergise.geopedia.core.style.model.NumberSpec;

public final class LineWidthOf extends NumberSpec
{
	public LineStyleSpec lineStyle;
	
	public LineWidthOf()
	{
		//...
	}
	
	public LineWidthOf(LineStyleSpec lineStyle)
	{
		this.lineStyle = lineStyle;
	}
	
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(LineWidthOf.class+" not supported");	
	}

	public void toString(StringBuffer sb)
	{
		sb.append('(');
		lineStyle.toString(sb);
		sb.append(").width");
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			lineStyle.accept(v);
			
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return lineStyle.isConst();
	}
	
	public Object clone()
	{
		return new LineWidthOf((LineStyleSpec) lineStyle.clone());
	}
}
