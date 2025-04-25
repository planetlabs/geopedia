package com.sinergise.geopedia.core.style.strings;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.StringSpec;

public final class StringConcat extends StringSpec
{
	public StringSpec left;
	public StringSpec right;
	
	public StringConcat()
	{
		// ..
	}
	
	public StringConcat(StringSpec left, StringSpec right)
	{
		this.left = left;
		this.right = right;
	}
	
	public void toString(StringBuffer sb)
	{
		sb.append('(');
		left.toString(sb);
		sb.append(") + (");
		right.toString(sb);
		sb.append(')');
	}
	
	@Override
	public void toStringJS(StringBuffer sb) {
		sb.append('(');
		left.toStringJS(sb);
		sb.append(") + (");
		right.toStringJS(sb);
		sb.append(')');
	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			left.accept(v);
			right.accept(v);
			
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
	    return left.isConst() && right.isConst();
	}
	
	public Object clone()
	{
		return new StringConcat((StringSpec)left.clone(), (StringSpec)right.clone());
	}
}
