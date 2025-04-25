package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.BooleanSpec;
import com.sinergise.geopedia.core.style.model.TextStyleSpec;

public final class ItalicOf extends BooleanSpec
{
	public TextStyleSpec textStyle;

	public ItalicOf()
	{
		// ...
	}

	public ItalicOf(TextStyleSpec textStyle)
	{
		this.textStyle = textStyle;
	}
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(ItalicOf.class+" not supported");	
	}

	
	public void toString(StringBuffer sb)
	{
		sb.append('(');
		textStyle.toString(sb);
		sb.append(").italic");
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
		return new ItalicOf((TextStyleSpec) textStyle.clone());
	}
}
