package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.StringSpec;
import com.sinergise.geopedia.core.style.model.SymbolStyleSpec;

public final class SymbolTextOf extends StringSpec
{
	public SymbolStyleSpec symbolStyle;

	public SymbolTextOf()
	{
		// ...
	}

	public SymbolTextOf(SymbolStyleSpec symbolStyle)
	{
		this.symbolStyle = symbolStyle;
	}
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(SymbolTextOf.class+" not supported");	
	}

	
	public void toString(StringBuffer sb)
	{
		sb.append('(');
		symbolStyle.toString(sb);
		sb.append(").text");
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
		return new SymbolTextOf((SymbolStyleSpec) symbolStyle.clone());
	}
}
