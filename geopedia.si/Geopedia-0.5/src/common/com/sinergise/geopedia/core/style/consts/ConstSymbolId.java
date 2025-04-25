package com.sinergise.geopedia.core.style.consts;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.SymbolId;
import com.sinergise.geopedia.core.style.model.SymbolIdSpec;

public class ConstSymbolId extends SymbolIdSpec
{
	public int symbolId;
	
	public ConstSymbolId()
	{
		
	}
	
	public ConstSymbolId(int symbolId)
	{
		if (symbolId < SymbolId.MIN_VALID_ID) //XXX || symbolId > SymbolId.MAX_VALID_ID)
			this.symbolId = SymbolId.NONE;
		else
			this.symbolId = symbolId;
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true))
			v.visit(this, false);
	}
	
	public boolean isConst()
	{
		return true;
	}
	
	public void toStringJS(StringBuffer sb) {
		sb.append(symbolId);
	}
	public void toString(StringBuffer sb)
	{
		sb.append("symbolId(");
		sb.append(symbolId);
		sb.append(')');
	}
	
	public Object clone()
	{
		return new ConstSymbolId(symbolId);
	}
}
