package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.ColorSpec;
import com.sinergise.geopedia.core.style.model.FillStyleSpec;

public final class FgColorOf extends ColorSpec
{
	public FillStyleSpec fillStyle;
	
	public FgColorOf()
	{
		// ...
	}
	
	public FgColorOf(FillStyleSpec fillStyle)
	{
		this.fillStyle = fillStyle;
	}
	
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(FgColorOf.class+" not supported");	
	}

	public void toString(StringBuffer sb)
	{
		sb.append('(');
		fillStyle.toString(sb);
		sb.append(").fgColor");
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			fillStyle.accept(v);
			
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return fillStyle.isConst();
	}
	
	public Object clone()
	{
		return new FgColorOf((FillStyleSpec) fillStyle.clone());
	}
}
