package com.sinergise.geopedia.core.style.numbers;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.Sym;
import com.sinergise.geopedia.core.style.model.BooleanSpec;
import com.sinergise.geopedia.core.style.model.NumberSpec;

public final class CompareNum extends BooleanSpec
{
	public NumberSpec left, right;
	public int op;

	public CompareNum()
	{
		// ...
	}

	public CompareNum(NumberSpec left, int op, NumberSpec right)
	{
		switch (op) {
		case Sym.LESS:
		case Sym.LESS_EQ:
		case Sym.GREATER:
		case Sym.GREATER_EQ:
		case Sym.EQUALS:
		case Sym.NOT_EQUALS:
			break;
		default:
			throw new IllegalStateException();
		}

		this.left = left;
		this.op = op;
		this.right = right;
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
	public void toStringJS(StringBuffer sb) {
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
		return new CompareNum((NumberSpec)left.clone(), op, (NumberSpec)right.clone());
	}
}
