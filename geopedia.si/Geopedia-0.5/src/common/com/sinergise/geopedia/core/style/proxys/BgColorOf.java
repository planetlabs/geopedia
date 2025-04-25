package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.ColorSpec;
import com.sinergise.geopedia.core.style.model.FillStyleSpec;

public final class BgColorOf extends ColorSpec
{
	public FillStyleSpec fillStyle;
	
	public BgColorOf()
	{
		// ...
	}
	
	public BgColorOf(FillStyleSpec fillStyle)
	{
		this.fillStyle = fillStyle;
	}
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(BgColorOf.class+" not supported");	
	}

	public void toString(StringBuffer sb)
	{
		sb.append('(');
		fillStyle.toString(sb);
		sb.append(").bgColor");
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
		return new BgColorOf((FillStyleSpec)fillStyle.clone());
	}
}
