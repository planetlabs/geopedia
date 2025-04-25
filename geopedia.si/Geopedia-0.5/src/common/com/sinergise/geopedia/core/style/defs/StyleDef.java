package com.sinergise.geopedia.core.style.defs;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.FillStyleSpec;
import com.sinergise.geopedia.core.style.model.LineStyleSpec;
import com.sinergise.geopedia.core.style.model.StyleSpec;
import com.sinergise.geopedia.core.style.model.SymbolStyleSpec;
import com.sinergise.geopedia.core.style.model.TextStyleSpec;

public final class StyleDef extends StyleSpec
{
	public LineStyleSpec line;
	public FillStyleSpec fill;
	public SymbolStyleSpec sym;
	public TextStyleSpec text;
	
	public StyleDef()
	{
		// ...
	}
	
	public StyleDef(LineStyleSpec line, FillStyleSpec fill, SymbolStyleSpec sym, TextStyleSpec text)
    {
		this.line = line;
		this.fill = fill;
		this.sym = sym;
		this.text = text;
    }

	
	@Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(StyleDef.class+" not supported");	
	}
	public void toString(StringBuffer sb)
	{
		sb.append("style(");
		line.toString(sb);
		sb.append(", ");
		fill.toString(sb);
		sb.append(", ");
		sym.toString(sb);
		sb.append(", ");
		text.toString(sb);
		sb.append(')');
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			line.accept(v);
			fill.accept(v);
			sym.accept(v);
			text.accept(v);
			
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return fill.isConst() && line.isConst() && sym.isConst() && text.isConst();
	}
	
	public Object clone()
	{
		return new StyleDef((LineStyleSpec)line.clone(), (FillStyleSpec)fill.clone(), (SymbolStyleSpec)sym.clone(), (TextStyleSpec)text.clone());
	}
}
