package com.sinergise.geopedia.style.processor.extra;

import com.sinergise.geopedia.core.entities.walk.UserFieldPath;
import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.StringSpec;

public final class SymbolTextFromField extends StringSpec
{
	public UserFieldPath field;

	public SymbolTextFromField(UserFieldPath fieldPath)
    {
		this.field = fieldPath;
    }

	public void accept(StyleVisitor v)
    {
		if (v.visit(this, true))
			v.visit(this, false);
    }

	public boolean isConst()
    {
	    return false;
    }
	
	@Override
	public void toStringJS(StringBuffer sb) {
		throw new IllegalStateException();	
	}

	public void toString(StringBuffer sb)
    {
		field.toString(sb);
		sb.append(".symbolStyle.text");
    }
	
	public Object clone()
	{
		return new SymbolTextFromField(field);
	}
}
