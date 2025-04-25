package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.ColorSpec;
import com.sinergise.geopedia.core.style.model.SymbolStyleSpec;

public final class SymbolColorOf extends ColorSpec
{
	public SymbolStyleSpec symbolStyle;
	
	public SymbolColorOf()
	{
		// ...
	}
	
	public SymbolColorOf(SymbolStyleSpec symbolStyle)
	{
		this.symbolStyle = symbolStyle;
	}
	
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(SymbolColorOf.class+" not supported");	
	}

	public void toString(StringBuffer sb)
	{
		sb.append('(');
		symbolStyle.toString(sb);
		sb.append(").color");
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
		return new SymbolColorOf((SymbolStyleSpec) symbolStyle.clone());
	}
}
