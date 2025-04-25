package com.sinergise.geopedia.core.style.bools;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.Sym;
import com.sinergise.geopedia.core.style.model.BooleanSpec;

public final class AndOrXor extends BooleanSpec
{
	public BooleanSpec left, right;
	public int type;
	
	public AndOrXor() 
	{
		// ...
    }
    
	public AndOrXor(BooleanSpec left, int type, BooleanSpec right)
	{
		this.left = left;
		this.right = right;
		this.type = type;
	}
	
	public void toStringJS(StringBuffer sb) {
		sb.append('(');
		left.toStringJS(sb);
		
		sb.append(") ");
		sb.append(Sym.Util.toString(type));
		sb.append(" (");
		
		right.toStringJS(sb);
		sb.append(')');
	}
	
	public void toString(StringBuffer sb)
	{
		sb.append('(');
		left.toString(sb);
		
		sb.append(") ");
		sb.append(Sym.Util.toString(type));
		sb.append(" (");
		
		right.toString(sb);
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
		return new AndOrXor((BooleanSpec)left.clone(), type, (BooleanSpec)right.clone());
	}
}
