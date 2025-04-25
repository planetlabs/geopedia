package com.sinergise.geopedia.core.style.proxys;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.BooleanSpec;
import com.sinergise.geopedia.core.style.model.TextStyleSpec;

public final class BoldOf extends BooleanSpec
{
	public TextStyleSpec textStyle;

	public BoldOf()
	{
		// ...
	}

	public BoldOf(TextStyleSpec textStyle)
	{
		this.textStyle = textStyle;
	}
	
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(BoldOf.class+" not supported");	
	}

	
	public void toString(StringBuffer sb)
	{
		sb.append('(');
		textStyle.toString(sb);
		sb.append(").bold");
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
		return new BoldOf((TextStyleSpec)textStyle.clone());
	}
}
