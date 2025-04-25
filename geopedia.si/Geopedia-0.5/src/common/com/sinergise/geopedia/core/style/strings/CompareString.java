package com.sinergise.geopedia.core.style.strings;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.Sym;
import com.sinergise.geopedia.core.style.model.BooleanSpec;
import com.sinergise.geopedia.core.style.model.StringSpec;

public final class CompareString extends BooleanSpec
{
	public StringSpec left, right;
	public int op;

	public CompareString()
	{
		// ...
	}

	public CompareString(StringSpec left, int op, StringSpec right)
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
		return new CompareString((StringSpec)left.clone(), op, (StringSpec)right.clone());
	}
}
