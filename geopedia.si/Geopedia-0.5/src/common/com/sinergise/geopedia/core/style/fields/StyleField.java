package com.sinergise.geopedia.core.style.fields;

import java.util.HashSet;

import com.sinergise.geopedia.core.entities.walk.FieldPath;
import com.sinergise.geopedia.core.entities.walk.UserFieldPath;
import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.FieldReader;
import com.sinergise.geopedia.core.style.model.StyleSpec;

public final class StyleField extends StyleSpec implements FieldReader
{
	public UserFieldPath fieldPath;
	
	public StyleField()
	{
		// ...
	}

	public StyleField(UserFieldPath fieldPath)
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
	
	public void getNeededFields(HashSet needFields)
	{
		// I need me
		needFields.add(fieldPath);
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
		return new StyleField(fieldPath);
	}
}
