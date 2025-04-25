package com.sinergise.geopedia.style.processor.extra;

import com.sinergise.geopedia.core.entities.walk.UserFieldPath;
import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.ColorSpec;

public class FillBgColorFromField extends ColorSpec
{
	public UserFieldPath field;

	public FillBgColorFromField(UserFieldPath fieldPath)
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
		throw new RuntimeException(FillBgColorFromField.class+" not supported");		
	}
	public void toString(StringBuffer sb)
    {
		field.toString(sb);
		sb.append(".fillStyle.bgColor");
    }
}
