package com.sinergise.geopedia.core.style.defs;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.ColorSpec;
import com.sinergise.geopedia.core.style.model.NumberSpec;
import com.sinergise.geopedia.core.style.model.StringSpec;
import com.sinergise.geopedia.core.style.model.SymbolIdSpec;
import com.sinergise.geopedia.core.style.model.SymbolStyleSpec;

public final class SymbolStyleDef extends SymbolStyleSpec
{
	public SymbolIdSpec symbolId;
	public NumberSpec size;
	public ColorSpec color;
	public StringSpec text;
	
	public SymbolStyleDef()
	{
		//..
	}
	
	public SymbolStyleDef(SymbolIdSpec symbolId, NumberSpec size, ColorSpec color, StringSpec text)
	{
		this.symbolId = symbolId;
		this.size = size;
		this.color = color;
		this.text = text;
	}
	
	public SymbolIdSpec getSymbolIdSpec()
	{
		return symbolId;
	}
	
	public StringSpec getTextSpec()
	{
		return text;
	}
	
	public NumberSpec getSizeSpec()
	{
		return size;
	}
	
	public ColorSpec getColorSpec()
	{
		return color;
	}
	
	
	@Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(SymbolStyleDef.class+" not supported");	
	}
	
	public void toString(StringBuffer sb)
	{
		sb.append("symbolStyle(");
		symbolId.toString(sb);
		sb.append(", ");
		size.toString(sb);
		sb.append(", ");
		color.toString(sb);
		sb.append(", ");
		text.toString(sb);
		sb.append(')');
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			color.accept(v);
			size.accept(v);
			symbolId.accept(v);
			text.accept(v);
			
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return color.isConst() && size.isConst() && symbolId.isConst() && text.isConst();
	}
	
	public Object clone()
	{
		return new SymbolStyleDef((SymbolIdSpec)symbolId.clone(), (NumberSpec)size.clone(), (ColorSpec)color.clone(), (StringSpec)text.clone());
	}
}
