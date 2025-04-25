package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.FontIdSpec;
import com.sinergise.geopedia.core.style.model.TextStyleSpec;

public final class FontIdOf extends FontIdSpec
{
	public TextStyleSpec textStyle;

	public FontIdOf()
	{
		// ...
	}

	public FontIdOf(TextStyleSpec textStyle)
	{
		this.textStyle = textStyle;
	}
	
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(FontIdOf.class+" not supported");	
	}

	public void toString(StringBuffer sb)
	{
		sb.append('(');
		textStyle.toString(sb);
		sb.append(").fontId");
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
		return new FontIdOf((TextStyleSpec) textStyle.clone());
	}
}
