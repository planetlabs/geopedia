package com.sinergise.geopedia.core.style.ternaries;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.BooleanSpec;
import com.sinergise.geopedia.core.style.model.StringSpec;

public final class StringTernary extends StringSpec
{
	public BooleanSpec condition;
	public StringSpec ifTrue;
	public StringSpec ifFalse;
	
	public StringTernary()
	{
		// ...
	}
	
	public StringTernary(BooleanSpec condition, StringSpec ifTrue, StringSpec ifFalse)
	{
		this.condition = condition;
		this.ifTrue = ifTrue;
		this.ifFalse = ifFalse;
	}
	
	public void toString(StringBuffer sb)
	{
		condition.toString(sb);
		sb.append(" ? ");
		ifTrue.toString(sb);
		sb.append(" : ");
		ifFalse.toString(sb);
	}
	
	@Override
	public void toStringJS(StringBuffer sb) {
		condition.toStringJS(sb);
		sb.append(" ? ");
		ifTrue.toStringJS(sb);
		sb.append(" : ");
		ifFalse.toStringJS(sb);

	}
	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			condition.accept(v);
			ifTrue.accept(v);
			ifFalse.accept(v);
			
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return condition.isConst() && ifTrue.isConst() && ifFalse.isConst(); // TODO this is too weak
	}
	
	public Object clone()
	{
		return new StringTernary((BooleanSpec)condition.clone(), (StringSpec)ifTrue.clone(), (StringSpec)ifFalse.clone());
	}
}
