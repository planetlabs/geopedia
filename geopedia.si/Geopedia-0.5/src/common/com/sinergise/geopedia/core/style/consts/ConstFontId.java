package com.sinergise.geopedia.core.style.consts;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.FontId;
import com.sinergise.geopedia.core.style.model.FontIdSpec;

public class ConstFontId extends FontIdSpec
{
	public int fontId;
	
	public ConstFontId()
	{
		
	}
	
	public ConstFontId(int fontId)
	{
		if (fontId < FontId.MIN_VALID_ID || fontId > FontId.MAX_VALID_ID)
			this.fontId = FontId.NONE;
		else
			this.fontId = fontId;
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
	
	@Override
	public void toStringJS(StringBuffer sb) {
		sb.append("SansSerif");
		
	}
	
	public void toString(StringBuffer sb)
	{
		sb.append("fontId(");
		sb.append(fontId);
		sb.append(')');
	}
	
	public Object clone()
	{
		return new ConstFontId(fontId);
	}
}
