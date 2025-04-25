package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.StyleSpec;
import com.sinergise.geopedia.core.style.model.SymbolStyleSpec;

public final class SymbolStyleOf extends SymbolStyleSpec
{
	public StyleSpec style;
	
	public SymbolStyleOf()
	{
		// ...
	}
	
	public SymbolStyleOf(StyleSpec style)
	{
		this.style = style;
	}
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(SymbolStyleOf.class+" not supported");	
	}

	
	public void toString(StringBuffer sb)
	{
		sb.append('(');
		style.toString(sb);
		sb.append(").symbolStyle");
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
		return new SymbolStyleOf((StyleSpec) style.clone());
	}
}
