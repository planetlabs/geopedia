package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.ColorSpec;
import com.sinergise.geopedia.core.style.model.TextStyleSpec;

public final class FontColorOf extends ColorSpec
{
	public TextStyleSpec textStyle;
	
	public FontColorOf()
	{
		// ...
	}
	
	public FontColorOf(TextStyleSpec textStyle)
	{
		this.textStyle = textStyle;
	}
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(FontColorOf.class+" not supported");	
	}

	public void toString(StringBuffer sb)
	{
		sb.append('(');
		textStyle.toString(sb);
		sb.append(").color");
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			textStyle.accept(v);
			
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return textStyle.isConst();
	}
	
	public Object clone()
	{
		return new FontColorOf((TextStyleSpec) textStyle.clone());
	}
}
