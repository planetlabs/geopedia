package com.sinergise.geopedia.core.style.ternaries;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.BooleanSpec;
import com.sinergise.geopedia.core.style.model.ColorSpec;

public final class ColorTernary extends ColorSpec
{
	public BooleanSpec condition;
	public ColorSpec ifTrue;
	public ColorSpec ifFalse;
	
	public ColorTernary()
	{
		// ...
	}
	
	public ColorTernary(BooleanSpec condition, ColorSpec ifTrue, ColorSpec ifFalse)
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
		return new ColorTernary((BooleanSpec)condition.clone(), (ColorSpec)ifTrue.clone(), (ColorSpec) ifFalse.clone());
	}
}
