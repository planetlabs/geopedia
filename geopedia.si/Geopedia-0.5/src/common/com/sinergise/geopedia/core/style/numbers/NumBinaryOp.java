package com.sinergise.geopedia.core.style.numbers;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.Sym;
import com.sinergise.geopedia.core.style.model.NumberSpec;

public final class NumBinaryOp extends NumberSpec
{
	public int op;
	public NumberSpec left;
	public NumberSpec right;
	
	public NumBinaryOp()
	{
		// ...
	}
	
	public NumBinaryOp(NumberSpec left, int op, NumberSpec right)
	{
		switch(op) { 
		case Sym.AMP:
		case Sym.BAR:
		case Sym.CAR:
		case Sym.MINUS:
		case Sym.PERCENT:
		case Sym.PLUS:
		case Sym.SHL:
		case Sym.SHR:
		case Sym.SHRU:
		case Sym.SLASH:
		case Sym.STAR:
			break;
		default:
			throw new IllegalArgumentException();
		}
		
		this.left = left;
		this.right = right;
		this.op = op;
	}
	
	public void toString(StringBuffer sb)
	{
		sb.append('(');
		left.toString(sb);
		sb.append(") ");
		sb.append(Sym.Util.toString(op));
		sb.append(" (");
		right.toString(sb);
		sb.append(')');
	}
	
	@Override
	public void toStringJS(StringBuffer sb)
	{
		sb.append('(');
		left.toStringJS(sb);
		sb.append(") ");
		sb.append(Sym.Util.toString(op));
		sb.append(" (");
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
		return new NumBinaryOp((NumberSpec) left.clone(), op, (NumberSpec)right.clone());
	}
}
