package com.sinergise.geopedia.core.style.ternaries;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.BooleanSpec;
import com.sinergise.geopedia.core.style.model.NumberSpec;

public final class NumericTernary extends NumberSpec
{
	public BooleanSpec condition;
	public NumberSpec ifTrue;
	public NumberSpec ifFalse;
	
	public NumericTernary()
	{
		// ...
	}
	
	public NumericTernary(BooleanSpec condition, NumberSpec ifTrue, NumberSpec ifFalse)
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
		return new NumericTernary((BooleanSpec)condition.clone(), (NumberSpec)ifTrue.clone(), (NumberSpec)ifFalse.clone());
	}
}
