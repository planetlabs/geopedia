package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.NumberSpec;
import com.sinergise.geopedia.core.style.model.SymbolStyleSpec;

public final class SymbolSizeOf extends NumberSpec
{
	public SymbolStyleSpec symbolStyle;

	public SymbolSizeOf()
	{
		// ...
	}

	public SymbolSizeOf(SymbolStyleSpec symbolStyle)
	{
		this.symbolStyle = symbolStyle;
	}
	
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(SymbolSizeOf.class+" not supported");	
	}

	public void toString(StringBuffer sb)
	{
		sb.append('(');
		symbolStyle.toString(sb);
		sb.append(").size");
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			symbolStyle.accept(v);
			
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return symbolStyle.isConst();
	}
	
	public Object clone()
	{
		return new SymbolSizeOf((SymbolStyleSpec)symbolStyle.clone());
	}
}
