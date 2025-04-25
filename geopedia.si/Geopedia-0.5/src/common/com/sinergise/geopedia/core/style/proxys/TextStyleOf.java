package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.StyleSpec;
import com.sinergise.geopedia.core.style.model.TextStyleSpec;

public final class TextStyleOf extends TextStyleSpec
{
	public StyleSpec style;
	
	public TextStyleOf()
	{
		// ...
	}
	
	public TextStyleOf(StyleSpec style)
	{
		this.style = style;
	}
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(TextStyleOf.class+" not supported");	
	}

	public void toString(StringBuffer sb)
	{
		sb.append('(');
		style.toString(sb);
		sb.append(").textStyle");
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
		return new TextStyleOf((StyleSpec) style.clone());
	}
}
