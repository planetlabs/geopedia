package com.sinergise.geopedia.style.processor.extra;

import com.sinergise.geopedia.core.entities.walk.UserFieldPath;
import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.NumberSpec;

public class LineWidthFromField extends NumberSpec
{
	public UserFieldPath field;

	public LineWidthFromField(UserFieldPath fieldPath)
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
		throw new RuntimeException(LineWidthFromField.class+" not supported");		
	}
	public void toString(StringBuffer sb)
    {
		field.toString(sb);
		sb.append(".lineStyle.width");
    }
}
