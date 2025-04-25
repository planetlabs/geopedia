package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.FillStyleSpec;
import com.sinergise.geopedia.core.style.model.FillTypeSpec;

public final class FillTypeOf extends FillTypeSpec
{
	public FillStyleSpec fillStyle;
	
	public FillTypeOf()
	{
		// ...
	}
	
	public FillTypeOf(FillStyleSpec fillStyle)
	{
		this.fillStyle = fillStyle;
	}
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(FillTypeOf.class+" not supported");	
	}

	public void toString(StringBuffer sb)
	{
		sb.append('(');
		fillStyle.toString(sb);
		sb.append(").type");
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
		return new FillTypeOf((FillStyleSpec) fillStyle.clone());
	}
}
