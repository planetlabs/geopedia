package com.sinergise.geopedia.core.style.fields;

import com.sinergise.geopedia.core.entities.walk.FieldPath;
import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.FieldReader;
import com.sinergise.geopedia.core.style.model.NumberSpec;

public final class NumberField extends NumberSpec implements FieldReader
{
	public FieldPath fieldPath;
	
	public NumberField()
	{
		// ...
	}

	public NumberField(FieldPath fieldPath)
	{
		this.fieldPath = fieldPath;
	}
	
	public void toString(StringBuffer sb)
	{
		fieldPath.toString(sb);
	}
	
	@Override
	public void toStringJS(StringBuffer sb) {
		fieldPath.toStringJS(sb);
		
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			v.visit(this, false);
		}
	}

	public FieldPath getFieldPath()
    {
		return fieldPath;
    }
	
	public boolean isConst()
	{
		return false;
	}
	
	public Object clone()
	{
		return new NumberField(fieldPath);
	}
}
