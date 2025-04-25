package com.sinergise.geopedia.core.style.ternaries;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.BooleanSpec;
import com.sinergise.geopedia.core.style.model.LineStyleSpec;

public final class LineStyleTernary extends LineStyleSpec
{
	public BooleanSpec condition;
	public LineStyleSpec ifTrue;
	public LineStyleSpec ifFalse;

	public LineStyleTernary()
	{
		// ...
	}

	public LineStyleTernary(BooleanSpec condition, LineStyleSpec ifTrue, LineStyleSpec ifFalse)
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
		return new LineStyleTernary((BooleanSpec)condition.clone(), (LineStyleSpec)ifTrue.clone(), (LineStyleSpec)ifFalse.clone());
	}
}
