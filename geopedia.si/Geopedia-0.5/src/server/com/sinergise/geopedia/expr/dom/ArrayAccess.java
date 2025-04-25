package com.sinergise.geopedia.expr.dom;

import com.sinergise.geopedia.core.style.Sym;

public class ArrayAccess extends BinaryOp
{
	public ArrayAccess(Expression arr, Expression index)
	{
		super(arr, Sym.LBRACKET, index);
	}
}
