package com.sinergise.geopedia.core.style.fields;

import com.sinergise.geopedia.core.entities.walk.FieldPath;
import com.sinergise.geopedia.core.entities.walk.UserFieldPath;
import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.FieldReader;
import com.sinergise.geopedia.core.style.model.StringSpec;

public final class StringField extends StringSpec implements FieldReader
{
	public UserFieldPath fieldPath;
	
	public StringField()
	{
		// ...
	}

	public StringField(UserFieldPath fieldPath)
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
		return new StringField(fieldPath);
	}
}
