package com.sinergise.geopedia.core.style.numbers;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.Sym;
import com.sinergise.geopedia.core.style.model.NumberSpec;

public final class NumUnaryOp extends NumberSpec
{
	public NumberSpec base;
	public int op;
	
	public NumUnaryOp()
	{
		// ...
	}
	
	public NumUnaryOp(int op, NumberSpec base)
	{
		switch(op) {
		case Sym.MINUS:
		case Sym.TILDE:
			break;
		default:
			throw new IllegalArgumentException();
		}
		
		this.op = op;
		this.base = base;
	}
	
	public void toString(StringBuffer sb)
	{
		sb.append(Sym.Util.toString(op));
		sb.append('(');
		base.toString(sb);
		sb.append(')');
	}
	
	@Override
	public void toStringJS(StringBuffer sb) {
		throw new RuntimeException(NumUnaryOp.class+" not supported");
	}
	

	
	public void accept(StyleVisitor v)
	{
		if (v.visit(this, true)) {
			base.accept(v);
			
			v.visit(this, false);
		}
	}
	
	public boolean isConst()
	{
		return base.isConst();
	}
	
	public Object clone()
	{
		return new NumUnaryOp(op, (NumberSpec) base.clone());
	}
}
