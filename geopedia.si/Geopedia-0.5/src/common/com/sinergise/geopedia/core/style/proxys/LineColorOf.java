package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.ColorSpec;
import com.sinergise.geopedia.core.style.model.LineStyleSpec;

public final class LineColorOf extends ColorSpec
{
	public LineStyleSpec lineStyle;
	
	public LineColorOf()
	{
		//...
	}
	
	public LineColorOf(LineStyleSpec lineStyle)
	{
		this.lineStyle = lineStyle;
	}
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(LineColorOf.class+" not supported");	
	}

	public void toString(StringBuffer sb)
	{
		sb.append('(');
		lineStyle.toString(sb);
		sb.append(").color");
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
		return new LineColorOf((LineStyleSpec)lineStyle.clone());
	}
}
