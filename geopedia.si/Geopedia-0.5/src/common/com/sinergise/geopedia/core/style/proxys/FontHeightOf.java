package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.NumberSpec;
import com.sinergise.geopedia.core.style.model.TextStyleSpec;

public final class FontHeightOf extends NumberSpec
{
	public TextStyleSpec textStyle;

	public FontHeightOf()
	{
		// ...
	}

	public FontHeightOf(TextStyleSpec textStyle)
	{
		this.textStyle = textStyle;
	}
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(FontHeightOf.class+" not supported");	
	}

	public void toString(StringBuffer sb)
	{
		sb.append('(');
		textStyle.toString(sb);
		sb.append(").height");
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
		return new FontHeightOf((TextStyleSpec) textStyle.clone());
	}
}
