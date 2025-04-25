package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.SymbolIdSpec;
import com.sinergise.geopedia.core.style.model.SymbolStyleSpec;

public final class SymbolIdOf extends SymbolIdSpec
{
	public SymbolStyleSpec symbolStyle;

	public SymbolIdOf()
	{
		// ...
	}

	public SymbolIdOf(SymbolStyleSpec symbolStyle)
	{
		this.symbolStyle = symbolStyle;
	}
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(SymbolIdOf.class+" not supported");	
	}

	
	public void toString(StringBuffer sb)
	{
		sb.append('(');
		symbolStyle.toString(sb);
		sb.append(").symbolId");
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
		return new SymbolIdOf((SymbolStyleSpec) symbolStyle.clone());
	}
}
