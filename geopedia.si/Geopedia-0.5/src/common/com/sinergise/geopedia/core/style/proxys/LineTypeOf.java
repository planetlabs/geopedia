package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.LineStyleSpec;
import com.sinergise.geopedia.core.style.model.LineTypeSpec;

public final class LineTypeOf extends LineTypeSpec
{
	public LineStyleSpec lineStyle;
	
	public LineTypeOf()
	{
		//...
	}
	
	public LineTypeOf(LineStyleSpec lineStyle)
	{
		this.lineStyle = lineStyle;
	}
	
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(LineTypeOf.class+" not supported");	
	}

	public void toString(StringBuffer sb)
	{
		sb.append('(');
		lineStyle.toString(sb);
		sb.append(").lineType");
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
		return new LineTypeOf((LineStyleSpec)lineStyle.clone());
	}
}
